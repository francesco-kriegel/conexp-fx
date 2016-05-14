package conexp.fx.core.algorithm.nextclosures;

import java.io.File;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conexp.fx.core.algorithm.nextclosures.NextClosures2.Result;
import conexp.fx.core.collections.BitSetSet2;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.Context;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.math.Math3;
import conexp.fx.core.util.Meter;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import conexp.fx.gui.task.TimeTask;
import conexp.fx.gui.util.Platform2;

public class NextClosures2Bit {

  private static final class BitResult {

    private final Set<BitConcept>          concepts     = Collections3.newConcurrentHashSet();
    private final Set<BitImplication>      implications = Collections3.newConcurrentHashSet();
    private final Map<BitSetSet2, Integer> candidates   = new ConcurrentHashMap<>();
    private final Set<BitSetSet2>          processed    = Collections3.newConcurrentHashSet();
    private int                            cardinality  = 0;

    private BitResult() {
      candidates.put(new BitSetSet2(), 0);
    }

    private final boolean isNewIntent(final BitSetSet2 s) {
      try {
        return processed.add(s);
      } catch (ConcurrentModificationException __) {
        return isNewIntent(s);
      }
    }

    private final void addNewCandidates(final int max, final BitSetSet2 intent) {
      // if (isNewIntent(intent))
      for (int m = 0; m < max; m++)
        if (!intent.contains(m)) {
          final BitSetSet2 candidateM = new BitSetSet2(intent);
          candidateM.add(m);
          candidates.put(candidateM, 0);
        }
    }
  }

  private static final class BitImplication extends Pair<BitSetSet2, BitSetSet2> {

    public BitImplication(final BitSetSet2 premise, final BitSetSet2 conclusion) {
      super(premise, conclusion);
    }
  }

  private static final class BitConcept extends Pair<BitSetSet2, BitSetSet2> {

    public BitConcept(final BitSetSet2 premise, final BitSetSet2 conclusion) {
      super(premise, conclusion);
    }
  }

  private static final Function<BitSetSet2, BitSetSet2>
      bitClosure(final Set<BitImplication> implications, final int c) {
    return set -> {
      final BitSetSet2 x = new BitSetSet2(set);
      // if (set instanceof BitSetSet2)
      // x = (BitSetSet2) set;
      // else {
      // x = new BitSetSet2();
      // x.addAll(set);
      // }
      boolean changed = false;
      final BitSetSet2 y = x.clone();
      // implications.stream().forEach(i -> {
      // if (x.size() > i.x().size() && x.containsAll(i.x()))
      // x.addAll(i.y());
      // });
      // for (BitImplication i : implications)
      // if (x.size() > i.x().size() && x.containsAll(i.x()))
      // x.addAll(i.y());
      implications
          .parallelStream()
          .filter(i -> i.x().size() >= c && x.containsAll(i.x()) && x.size() != i.x().size())
          // .filter(i -> x.containsAll(i.x()) && !i.x().containsAll(x))
          // && (x.size() < i.y().size() || !x.containsAll(i.y())))
          .map(i -> i.y())
          .sequential()
          .forEach(x::addAll);
      changed = !x.containsAll(y);
      while (changed) {
        final BitSetSet2 z = x.clone();
        // implications.stream().forEach(i -> {
        // if (x.size() > i.x().size() && x.containsAll(i.x()))
        // x.addAll(i.y());
        // });
        // for (BitImplication i : implications)
        // if (x.size() > i.x().size() && x.containsAll(i.x()))
        // x.addAll(i.y());
        implications
            .parallelStream()
            .filter(i -> x.containsAll(i.x()) && x.size() != i.x().size())
            // .filter(i -> x.containsAll(i.x()) && !i.x().containsAll(x))
            // && (x.size() < i.y().size() || !x.containsAll(i.y())))
            .map(i -> i.y())
            .sequential()
            .forEach(x::addAll);
        changed = !x.containsAll(z);
      };
      return x;
    };
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitBitCompute(final Context<G, M> cxt) {
    return bitBitCompute(cxt, Executors.newWorkStealingPool(), __ -> {} , __ -> {} , __ -> {} , __ -> {} , () -> false);
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitBitCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt instanceof MatrixContext ? (MatrixContext<G, M>) cxt : cxt.clone();
    final int maxC = mcxt.colHeads().size();
    final long start = System.nanoTime();
    final int maxR = mcxt.rowHeads().size();
    final List<BitSetSet2> rows = new ArrayList<>(maxR);
    for (int i = 0; i < maxR; i++)
      rows.add((BitSetSet2) mcxt._row(i));
    final List<BitSetSet2> cols = new ArrayList<>(maxC);
    for (int j = 0; j < maxC; j++)
      cols.add((BitSetSet2) mcxt._col(j));
    final Function<Integer, Supplier<BitSet>> g = t -> () -> {
      final BitSet b = new BitSet(t);
      b.set(0, t);
      return b;
    };
    final Function<Supplier<BitSet>, Function<Function<Integer, BitSetSet2>, Function<BitSetSet2, BitSetSet2>>> and =
        s -> f -> x -> {
          final BitSetSet2 y = new BitSetSet2();
          y.getBitSet().or(x.parallelStream().map(f).map(BitSetSet2::getBitSet).sequential().reduce(s.get(), (u, v) -> {
            u.and(v);
            return u;
          }));
          return y;
        };
    final Function<BitSetSet2, BitSetSet2> colAnd = and.apply(g.apply(maxR)).apply(cols::get);
    final Function<BitSetSet2, BitSetSet2> rowAnd = and.apply(g.apply(maxC)).apply(rows::get);
    System.out.println("context transformation took " + Math3.formatNanos(System.nanoTime() - start));
    final BitResult b = new BitResult();
//    final Function<BitSetSet2, BitSetSet2> cl = bitClosure(b.implications);
    for (; b.cardinality <= maxC; b.cardinality++) {
      if (isCancelled.get())
        break;
      final double q = ((double) b.cardinality) / ((double) maxC);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + b.cardinality + "/" + maxC + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> fs = Collections3.newConcurrentHashSet();
      b.candidates
          .keySet()
          .parallelStream()
          .filter(c -> c.size() == b.cardinality)
          .forEach(c -> fs.add(executor.submit(() -> {
            final BitSetSet2 d = bitClosure(b.implications, b.candidates.get(c)).apply(c);
            // if (c.size() == d.size()) {
            if (c.containsAll(d)) {
              final BitSetSet2 c1 = colAnd.apply(c);
              final BitSetSet2 c2 = rowAnd.apply(c1);
              if (b.isNewIntent(c2)) {
                b.concepts.add(new BitConcept(c1, new BitSetSet2(c2)));
                b.addNewCandidates(maxC, c2);
              }
              // if (c.size() != c2.size()) {
              if (!c.containsAll(c2)) {
                c2.removeAll(c);
                b.implications.add(new BitImplication(c, c2));
              }
            } else
              b.candidates.put(d, b.cardinality);
            b.candidates.remove(c);
          })));
      for (Future<?> f : fs)
        try {
          f.get();
        } catch (InterruptedException | ExecutionException __) {}
    }
    updateStatus.accept(b.concepts.size() + " concepts, and " + b.implications.size() + " implications found");
    final Result<G, M> r = new Result<>();
    b.concepts
        .parallelStream()
        .map(c -> new Concept<G, M>(mcxt.rowHeads().getAll(c.x(), true), mcxt.colHeads().getAll(c.y(), true)))
        .forEach(conceptConsumer.andThen(r.concepts::add));
    b.implications
        .parallelStream()
        .map(i -> new Implication<G, M>(mcxt.colHeads().getAll(i.x(), true), mcxt.colHeads().getAll(i.y(), true)))
        .forEach(implicationConsumer.andThen(r.implications::add));
    return Pair.of(r.concepts, r.implications);
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>> bitCompute(
      final Context<G, M> cxt,
      final ExecutorService executor,
      final Consumer<Concept<G, M>> conceptConsumer,
      final Consumer<Implication<G, M>> implicationConsumer,
      final Consumer<String> updateStatus,
      final Consumer<Double> updateProgress,
      final Supplier<Boolean> isCancelled) {
    final MatrixContext<G, M> mcxt = cxt instanceof MatrixContext ? (MatrixContext<G, M>) cxt : cxt.clone();
    final BitResult b = new BitResult();
//    final Function<BitSetSet2, BitSetSet2> cl = bitClosure(b.implications);
    final int max = mcxt.colHeads().size();
    for (; b.cardinality <= max; b.cardinality++) {
      if (isCancelled.get())
        break;
      final double q = ((double) b.cardinality) / ((double) max);
      final int p = (int) (100d * q);
      updateStatus.accept("current cardinality: " + b.cardinality + "/" + max + " (" + p + "%)");
      updateProgress.accept(q);
      final Set<Future<?>> fs = Collections3.newConcurrentHashSet();
      b.candidates
          .keySet()
          .parallelStream()
          .filter(c -> c.size() == b.cardinality)
          .forEach(c -> fs.add(executor.submit(() -> {
            final BitSetSet2 d = bitClosure(b.implications, b.candidates.get(c)).apply(c);
            // if (c.size() == d.size()) {
            if (c.containsAll(d)) {
              final BitSetSet2 c1 = (BitSetSet2) mcxt._colAnd(c);
              final BitSetSet2 c2 = (BitSetSet2) mcxt._rowAnd(c1);
              if (b.isNewIntent(c2)) {
                b.concepts.add(new BitConcept(c1, new BitSetSet2(c2)));
                b.addNewCandidates(max, c2);
              }
              // if (c.size() != c2.size()) {
              if (!c.containsAll(c2)) {
                c2.removeAll(c);
                b.implications.add(new BitImplication(c, c2));
              }
            } else
              b.candidates.put(d, b.cardinality);
            b.candidates.remove(c);
          })));
      for (Future<?> f : fs)
        try {
          f.get();
        } catch (InterruptedException | ExecutionException __) {}
    }
    updateStatus.accept(b.concepts.size() + " concepts, and " + b.implications.size() + " implications found");
    final Result<G, M> r = new Result<>();
    b.concepts
        .parallelStream()
        .map(c -> new Concept<G, M>(mcxt.rowHeads().getAll(c.x(), true), mcxt.colHeads().getAll(c.y(), true)))
        .forEach(conceptConsumer.andThen(r.concepts::add));
    b.implications
        .parallelStream()
        .map(i -> new Implication<G, M>(mcxt.colHeads().getAll(i.x(), true), mcxt.colHeads().getAll(i.y(), true)))
        .forEach(implicationConsumer.andThen(r.implications::add));
    return Pair.of(r.concepts, r.implications);
  }

  public static final <G, M> Pair<Set<Concept<G, M>>, Set<Implication<G, M>>>
      bitCompute(final Context<G, M> cxt, final ExecutorService executor) {
    return bitCompute(cxt, executor, __ -> {} , __ -> {} , System.out::println, System.out::println, () -> false);
  }

  public static void main(String[] args) throws Exception {
    System.out.println("starting context import...");
    final Meter<Long> m = Meter.newNanoStopWatch();
    final MatrixContext<String, String> cxt =
        CXTImporter.read(new File("/Users/francesco/workspace/Data/Datasets/SNOMED/snomed.cxt"));
    System.out.println("context import took " + Math3.formatNanos(m.measure()));
    bitCleanedCompute(
        cxt,
        Executors.newWorkStealingPool(),
        System.out::println,
        System.out::println,
        System.out::println,
        System.out::println,
        () -> false);
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
    final Pair<Set<Concept<Set<G>, Set<M>>>, Set<Implication<Set<G>, Set<M>>>> r =
        bitBitCompute(ccxt, executor, __ -> {} , __ -> {} , updateStatus, updateProgress, isCancelled);
    final Result<G, M> x = new Result<G, M>();
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
    return Pair.of(x.concepts, x.implications);
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
    final long s = System.nanoTime();
    System.out.print(".");
    mcxt.reduce();
    System.out.print(".");
    final MatrixContext<Set<G>, Set<M>> rcxt = mcxt.reduced.clone();
    final MatrixContext<Set<G>, Set<M>> ccxt = mcxt.cleaned.clone();
    System.out.println("reducing took " + Math3.formatNanos(System.nanoTime() - s));
    final Pair<Set<Concept<Set<G>, Set<M>>>, Set<Implication<Set<G>, Set<M>>>> r =
        bitCompute(rcxt, executor, __ -> {} , __ -> {} , updateStatus, updateProgress, isCancelled);
    final Result<G, M> x = new Result<>();
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
    return Pair.of(x.concepts, x.implications);
  }

  public static final <G, M> TimeTask<?> createTask(FCADataset<G, M> dataset) {
    return new TimeTask<Void>(dataset, "NextClosures") {

      @Override
      protected Void call() throws Exception {
        updateProgress(0d, 1d);
        if (isCancelled())
          return null;
        bitBitCompute(
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
