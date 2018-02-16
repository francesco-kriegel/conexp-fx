package conexp.fx.core.algorithm.nextclosures;

/*-
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conexp.fx.core.collections.BitSetFX;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.math.ClosureOperator;
import conexp.fx.core.math.Math3;
import conexp.fx.core.util.Meter;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;

public class NextClosures2Bit {

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt instanceof MatrixContext ? (MatrixContext<G, M>) cxt : cxt.clone();
    final int max = mcxt.colHeads().size();
    final NextClosuresState<Integer, Integer, BitSetFX> state = NextClosuresState.withBitSets(max);
//    final Function<BitSetFX, BitSetFX> cl = bitClosure(b.implications);
    for (; state.cardinality <= max; state.cardinality++) {
      if (isCancelled.get())
        break;
      final double q = ((double) state.cardinality) / ((double) max);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + state.cardinality + "/" + max + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> fs = Collections3.newConcurrentHashSet();
      final Set<BitSetFX> cc = state.getActualCandidates();
      cc.forEach(c -> fs.add(executor.submit(() -> {
        final BitSetFX d = ClosureOperator
            .implicativeClosure(state.implications, state.getFirstPremiseSize(c), true, true, true, BitSetFX::new, c);
        if (c.geq(d)) {
//        if (c.containsAll(d)) {
          final BitSetFX c1 = mcxt._colAnd(c);
          final BitSetFX c2 = mcxt._rowAnd(c1);
          if (state.isNewIntent(c2)) {
            state.concepts.add(new Concept<Integer, Integer>(c1, new BitSetFX(c2)));
            state.addNewCandidates(c2);
          }
          if (!c.geq(c2)) {
//          if (!c.containsAll(c2)) {
            c2.removeAll(c);
            state.implications.add(new Implication<Integer, Integer>(c, c2));
          }
        } else
          state.addCandidate(d);
//        state.candidates.remove(c);
      })));
      for (Future<?> f : fs)
        try {
          f.get();
        } catch (InterruptedException | ExecutionException __) {}
      state.candidates.keySet().removeAll(cc);
    }
    updateStatus.accept(state.concepts.size() + " concepts, and " + state.implications.size() + " implications found");
    final NextClosuresState<G, M, Set<M>> r = NextClosuresState.withHashSets(cxt.colHeads());
    state.concepts
        .parallelStream()
        .map(
            c -> new Concept<G, M>(
                mcxt.rowHeads().getAll(c.getExtent(), true),
                mcxt.colHeads().getAll(c.getIntent(), true)))
        .forEach(conceptConsumer.andThen(r.concepts::add));
    state.implications
        .parallelStream()
        .map(
            i -> new Implication<G, M>(
                mcxt.colHeads().getAll(i.getPremise(), true),
                mcxt.colHeads().getAll(i.getConclusion(), true)))
        .forEach(implicationConsumer.andThen(r.implications::add));
    return r.getResultAndDispose();
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitCompute(final Context<G, M> cxt) {
    return bitCompute(cxt, Executors.newWorkStealingPool(), __ -> {}, __ -> {}, __ -> {}, __ -> {}, () -> false);
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitCleanedCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt.toMatrixContext();
    System.out.println("starting context cleaning...");
    final Meter<Long> nsw = Meter.newNanoStopWatch();
    mcxt.clean();
    System.out.println("context cleaning took " + Math3.formatNanos(nsw.measure()));
    System.out.println("cloning cleaned context...");
    final Meter<Long> nsw2 = Meter.newNanoStopWatch();
    final MatrixContext<Set<G>, Set<M>> ccxt = mcxt.cleaned.clone();
    System.out.println("context cloning took " + Math3.formatNanos(nsw2.measure()));

    final Meter<Long> nsw3 = Meter.newNanoStopWatch();
    final Pair<Set<Concept<Set<G>, Set<M>>>, Set<Implication<Set<G>, Set<M>>>> r =
        bitCompute(ccxt, executor, __ -> {}, __ -> {}, updateStatus, updateProgress, isCancelled);
    System.out.println("bitcleaned: " + nsw3.measureAndFormat());
    final Meter<Long> nsw4 = Meter.newNanoStopWatch();
    final NextClosuresState<G, M, Set<M>> x = NextClosuresState.withHashSets(cxt.colHeads());
    r
        .first()
        .parallelStream()
        .map(
            c -> new Concept<G, M>(
                c.extent().parallelStream().flatMap(Set::parallelStream).collect(Collectors.toSet()),
                c.intent().parallelStream().flatMap(Set::parallelStream).collect(Collectors.toSet())))
        .forEach(conceptConsumer.andThen(x.concepts::add));
    final Map<Set<M>, M> f = new ConcurrentHashMap<>();
    ccxt.colHeads().parallelStream().map(c -> new HashSet<M>(c)).forEach(c -> f.put(c, c.iterator().next()));
    r
        .second()
        .parallelStream()
        .map(
            i -> new Implication<G, M>(
                i.getPremise().parallelStream().map(f::get).collect(Collectors.toSet()),
                i.getConclusion().parallelStream().map(f::get).collect(Collectors.toSet())))
        .forEach(implicationConsumer.andThen(x.implications::add));
    f
        .keySet()
        .parallelStream()
        .flatMap(
            c -> c.size() > 1 ? c.parallelStream().map(m -> new Implication<G, M>(Collections.singleton(m), c))
                : Stream.empty())
        .forEach(implicationConsumer.andThen(x.implications::add));
    System.out.println("transform: " + nsw3.measureAndFormat());
    return x.getResultAndDispose();
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitReducedCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt.toMatrixContext();
//    final long s = System.nanoTime();
//    System.out.print(".");
    mcxt.reduce();
//    System.out.print(".");
    final MatrixContext<Set<G>, Set<M>> rcxt = mcxt.reduced.clone();
    final MatrixContext<Set<G>, Set<M>> ccxt = mcxt.cleaned.clone();
//    System.out.println("reducing took " + Math3.formatNanos(System.nanoTime() - s));
    final Pair<Set<Concept<Set<G>, Set<M>>>, Set<Implication<Set<G>, Set<M>>>> r =
        bitCompute(rcxt, executor, __ -> {}, __ -> {}, updateStatus, updateProgress, isCancelled);
    final NextClosuresState<G, M, Set<M>> x = NextClosuresState.withHashSets(cxt.colHeads());
    final Map<Set<M>, Set<Set<M>>> irr = new ConcurrentHashMap<>();
    ccxt.colHeads().parallelStream().filter(atts -> !rcxt.colHeads().contains(atts)).forEach(
        atts -> irr.put(atts, new HashSet<Set<M>>(ccxt.attributeQuasiOrder().col(atts))));
    final Map<Set<G>, Set<Set<G>>> jrr = new ConcurrentHashMap<>();
    ccxt.rowHeads().parallelStream().filter(objs -> !rcxt.rowHeads().contains(objs)).forEach(
        objs -> jrr.put(objs, new HashSet<Set<G>>(ccxt.objectQuasiOrder().col(objs))));
    final Map<Set<M>, M> f = new ConcurrentHashMap<>();
    ccxt.colHeads().parallelStream().map(c -> new HashSet<M>(c)).forEach(c -> f.put(c, c.iterator().next()));
    r
        .first()
        .parallelStream()
        .map(
            c -> new Concept<G, M>(
                c.extent().parallelStream().flatMap(Set::parallelStream).collect(Collectors.toSet()),
                c.intent().parallelStream().flatMap(Set::parallelStream).collect(Collectors.toSet())))
        .forEach(conceptConsumer.andThen(x.concepts::add));
    for (Concept<G, M> c : x.concepts) {
      for (Entry<Set<M>, Set<Set<M>>> e : irr.entrySet())
        if (c.intent().containsAll(e.getValue().parallelStream().map(f::get).collect(Collectors.toSet())))
          c.intent().addAll(e.getKey());
      for (Entry<Set<G>, Set<Set<G>>> e : jrr.entrySet())
        if (c.extent().containsAll(e.getValue().parallelStream().map(f::get).collect(Collectors.toSet())))
          c.extent().addAll(e.getKey());
    }
    r
        .second()
        .parallelStream()
        .map(
            i -> new Implication<G, M>(
                i.getPremise().parallelStream().map(f::get).collect(Collectors.toSet()),
                i.getConclusion().parallelStream().map(f::get).collect(Collectors.toSet())))
        .forEach(implicationConsumer.andThen(x.implications::add));
    f
        .keySet()
        .parallelStream()
        .flatMap(
            c -> c.size() > 1 ? c.parallelStream().map(m -> new Implication<G, M>(Collections.singleton(m), c))
                : Stream.empty())
        .forEach(implicationConsumer.andThen(x.implications::add));
    irr
        .keySet()
        .parallelStream()
        .flatMap(
            ir -> Stream.<Implication<G, M>> of(
                new Implication<G, M>(
                    Collections.singleton(f.get(ir)),
                    irr.get(ir).parallelStream().map(f::get).collect(Collectors.toSet())),
                new Implication<G, M>(
                    irr.get(ir).parallelStream().map(f::get).collect(Collectors.toSet()),
                    Collections.singleton(f.get(ir)))))
        .forEach(implicationConsumer.andThen(x.implications::add));
    return x.getResultAndDispose();
  }

  public static final <G, M> TimeTask<?> createTask(FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "NextClosures") {

      @Override
      protected Void call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        bitCompute(
            dataset.context.getSelection(),
            ConExpFX.instance.executor.tpe,
            concept -> Platform2.runOnFXThread(() -> dataset.concepts.add(concept)),
            implication -> Platform2.runOnFXThread(() -> dataset.implications.add(implication)),
            // dataset.concepts::add,
            // dataset.implications::add,
            status -> updateMessage(status),
            progress -> updateProgress(progress, 1d),
            () -> isCancelled());
        updateProgress(1d, 1d);
        return null;
      }
    };
  }

}
