package conexp.fx.core;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosure.NextClosure;
import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.core.closureoperators.ClosureOperator;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.ImplicationSet;
import de.tudresden.inf.tcs.fcalib.Implication;

public class NextClosureTest {

  private Map<String, Long> testResults  = new HashMap<String, Long>();
  private Map<String, Long> testResults2 = new HashMap<String, Long>();

  @Test
  public void test() {
    final String prefix = "../contexts/small/";
    final String[] filenames = new File(prefix).list();
//    final Set<String> filenames =
//        Sets.newHashSet(
//            "../contexts/scales/interordinal_5.cxt",
//            "../contexts/scales/contranominal_5.cxt",
//            "../contexts/small/tealady.cxt",
//            "../contexts/small/tamari_5.cxt",
//            "../contexts/small/.cxt",
//            "../contexts/small/verbandseigenschaften.cxt");
    for (String filename : filenames)
      testAgainstOldImplementationForExtents(prefix + filename);
//      testAgainstOldImplementationForImplications(prefix + filename);
    long ctime1 = 0;
    long ctime2 = 0;
    for (String filename : filenames) {
      final Long time1 = testResults.get(prefix + filename);
      final Long time2 = testResults2.get(prefix + filename);
      ctime1 += time1;
      ctime2 += time2;
      System.out.println(time1 + " :: " + time2);
    }
    System.out.println("<<< " + ctime1 + " :: " + ctime2 + " >>>");
  }

  @SuppressWarnings("deprecation")
  public void testAgainstOldImplementationForExtents(final String filename) {
    final MatrixContext<String, String> cxt = TestContexts.fromFile(new File(filename));
    long startTime = System.currentTimeMillis();
    final NextConcept<String, String> ne = new NextConcept<String, String>(cxt);
    final HashSet<Set<String>> nes =
        Sets.newHashSet(Iterators.transform(ne.iterator(), new Function<Concept<String, String>, Set<String>>() {

          @Override
          public Set<String> apply(Concept<String, String> input) {
            return input.getExtent();
          }
        }));
    final long time1 = System.currentTimeMillis() - startTime;
    testResults.put(filename, time1);
    startTime = System.currentTimeMillis();
    final NextClosure<String> nc = new NextClosure<String>(cxt.rowHeads(), new ClosureOperator<String>() {

      @Override
      public Set<String> closure(Set<String> s) {
        return cxt.extent(s);
      }

      @Override
      public boolean isClosed(Set<String> s) {
        return s.equals(closure(s));
      }

//      @Override
//      public Set<Set<String>> allClosures() {
//        throw new UnsupportedOperationException();
//      }
      @Override
      public boolean close(Set<String> set) {
        return !set.addAll(closure(set));
      }
    });
    final HashSet<Set<String>> ncs = Sets.newHashSet(nc.iterator());
    final long time2 = System.currentTimeMillis() - startTime;
    testResults2.put(filename, time2);
    System.out.println(filename + " >>> " + time1 + " .. " + time2);
    Assert.assertEquals(nes.size(), ncs.size());
    Assert.assertEquals(nes, ncs);
  }

  public void testAgainstOldImplementationForImplications(final String filename) {
    final MatrixContext<String, String> cxt = TestContexts.fromFile(new File(filename));
    long startTime = System.currentTimeMillis();
    final NextImplication<String, String> ni = new NextImplication<String, String>(cxt);
    final HashSet<Implication<String>> nis = Sets.newHashSet(ni.iterator());
    final long time1 = System.currentTimeMillis() - startTime;
    testResults.put(filename, time1);
    startTime = System.currentTimeMillis();
    final ImplicationSet<String> ncs = new ImplicationSet<String>();
    final NextClosure<String> nc = new NextClosure<String>(cxt.colHeads(), new ClosureOperator<String>() {

      @Override
      public boolean isClosed(Set<String> set) {
        return ncs.isClosed(set);
      }

      @Override
      public boolean close(Set<String> set) {
        return !set.addAll(ncs.closure(set));
      }

      @Override
      public Set<String> closure(Set<String> set) {
        return ncs.closure(set);
      }
    });
    final Iterator<Set<String>> it = nc.iterator();
    while (it.hasNext()) {
      final Set<String> next = it.next();
      Implication<String> imp = new Implication<String>(next, cxt.intent(next));
      ncs.add(imp);
    }
    final long time2 = System.currentTimeMillis() - startTime;
    testResults2.put(filename, time2);
    System.out.println(filename + " >>> " + time1 + " .. " + time2);
    System.out.println(nis);
    System.out.println();
    System.out.println(ncs);
    Assert.assertEquals(nis.size(), ncs.size());
    Assert.assertEquals(nis, ncs);
  }

  @Test
  public void testAgainstDanielsSamples() {
    for (Entry<MatrixContext<String, String>, ImplicationSet<String>> sample : TestContexts
        .danielsImplicationTest()
        .data()
        .entrySet()) {
      MatrixContext<String, String> cxt = sample.getKey();
      final Set<Implication<String>> expected = sample.getValue();
      final int size = expected.size() + 1;
      if (size < 500 && size > 10) {
        System.out.println("computing " + size + " implications");
        final long start = System.currentTimeMillis();
        final ImplicationSet<String> actual = new ImplicationSet<String>();
        final NextClosure<String> nc = new NextClosure<String>(cxt.colHeads(), new ClosureOperator<String>() {

          @Override
          public boolean isClosed(Set<String> set) {
            return actual.isClosed(set);
          }

          @Override
          public boolean close(Set<String> set) {
            return !set.addAll(actual.closure(set));
          }

          @Override
          public Set<String> closure(Set<String> set) {
            return actual.closure(set);
          }

        });
        final Iterator<Set<String>> it = nc.iterator();
        while (it.hasNext()) {
          final Set<String> next = it.next();
          final Set<String> conclusion = Sets.newHashSet(cxt.intent(next));
          conclusion.removeAll(next);
          Implication<String> imp = new Implication<String>(next, conclusion);
          actual.add(imp);
        }
        System.out.println("..." + (System.currentTimeMillis() - start) + "ms");
        Assert.assertEquals(size, actual.size());
        Assert.assertEquals(expected, actual);
      }
    }
  }

}