package conexp.fx.core.collections;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.ujmp.core.collections.set.BitSetSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import conexp.fx.core.math.GuavaIsomorphism;

public final class Collections3 {

  public static final <E> Set<E> newConcurrentHashSet() {
    return Collections.newSetFromMap(new ConcurrentHashMap<E, Boolean>());
  }

  public static final <E extends Number & Comparable<? super E>> double sum(final Collection<? extends E> c) {
    double s = 0;
    for (E e : c)
      s += e.doubleValue();
    return s;
  }

  public static final <E extends Number & Comparable<? super E>> double avg(final Collection<? extends E> c) {
    return sum(c) / (double) c.size();
  }

  public static final <E> Collection<E> union(final Collection<Collection<E>> c) {
    return new AbstractCollection<E>() {

      public final Iterator<E> iterator() {
        return Iterables.concat(c).iterator();
      }

      public final int size() {
        int size = 0;
        for (Collection<? extends E> _c : c)
          size += _c.size();
        return size;
      }
    };
  }

  @SafeVarargs
  public static final <E> Collection<E> union(final Collection<E>... c) {
    return union(Arrays.asList(c));
  }

  public static final <E> Collection<E> union(final Collection<? extends E> c1, final Collection<? extends E> c2) {
    return new AbstractCollection<E>() {

      public final Iterator<E> iterator() {
        return Iterators.concat(c1.iterator(), c2.iterator());
      }

      public final int size() {
        return c1.size() + c2.size();
      }
    };
  }

  public static final <E> Collection<E> intersection(final Collection<E> c1, final Collection<? extends E> c2) {
    return Collections2.filter(c1, Predicates.in(c2));
  }

  public static final <E> Collection<E> difference(final Collection<E> c1, final Collection<? extends E> c2) {
    return Collections2.<E> filter(c1, Predicates.not(Predicates.in(c2)));
  }

  public static final <T, E> Set<E> transform(final Set<T> s, final GuavaIsomorphism<T, E> f) {
    return new AbstractSet<E>() {

      @Override
      public final Iterator<E> iterator() {
        return Iterators.transform(s.iterator(), f);
      }

      @Override
      public final int size() {
        return s.size();
      }
    };
  }

  public static final <E> E random(final Collection<? extends E> c, final Random rng) {
    if (c.isEmpty())
      throw new NoSuchElementException();
    final int i = rng.nextInt(c.size());
    if (c instanceof List) {
      final List<? extends E> l = (List<? extends E>) c;
      return l.get(i);
    }
    int j = 0;
    for (E e : c)
      if (j++ == i)
        return e;
    throw new NoSuchElementException();
  }

  public static final <E> E random(final Collection<? extends E> c, final Predicate<E> p, final Random rng) {
    E e;
    while (true) {
      e = random(c, rng);
      if (p.apply(e))
        break;
    }
    return e;
  }

  public static final <E extends Comparable<E>> List<E> sort(final Iterable<? extends E> i) {
    final ArrayList<E> l = Lists.newArrayList(i);
    Collections.sort(l);
    return l;
  }

  public static final <E> List<E> sort(final Iterable<? extends E> i, final Comparator<? super E> c) {
    final ArrayList<E> l = Lists.newArrayList(i);
    Collections.sort(l, c);
    return l;
  }

  public static final <E, T extends E> Collection<T> elementsBySubClass(final Collection<E> c, final Class<T> clazz) {
    return Collections2.transform(Collections2.filter(c, Predicates.instanceOf(clazz)), new Function<E, T>() {

      public final T apply(final E e) {
        return clazz.cast(e);
      }
    });
  }

  public static final <E> Function<Set<E>, Iterator<E>> setToIterator() {
    return new Function<Set<E>, Iterator<E>>() {

      public final Iterator<E> apply(final Set<E> it) {
        return it.iterator();
      }
    };
  }

  public static final <E> Set<E> fromIterator(final Supplier<Iterator<E>> its) {
    return new AbstractSet<E>() {

      @Override
      public Iterator<E> iterator() {
        return its.get();
      }

      @Override
      public int size() {
        return Iterators.size(iterator());
      }
    };
  }

  public static final <E> Iterable<E> iterable(final Iterator<E> it) {
    return new Iterable<E>() {

      @Override
      public Iterator<E> iterator() {
        return it;
      }
    };
  }

  public static final Predicate<Integer> isSmaller(final int n) {
    return new Predicate<Integer>() {

      public final boolean apply(final Integer m) {
        return m < n;
      }
    };
  }

  public static final <E> E firstElement(final Iterable<E> it) {
    return it.iterator().next();
  }

  public static final <E> Function<Iterable<E>, E> firstElement() {
    return new Function<Iterable<E>, E>() {

      public final E apply(final Iterable<E> it) {
        return it.iterator().next();
      }
    };
  }

  public static final BitSetSet newBitSetSet(final Collection<Integer> c) {
    final BitSetSet b = new BitSetSet();
    b.addAll(c);
    return b;
  }

  public static final BitSetSet2 newBitSetSet2(final Collection<Integer> c) {
    final BitSetSet2 b = new BitSetSet2();
    b.addAll(c);
    return b;
  }

  public static final <E> List<E> filter(final List<E> l, final Predicate<E> p) {
    return new AbstractList<E>() {

      public final E get(final int index) {
        return Iterables.get(Iterables.filter(l, p), index);
      }

      public final int size() {
        return Iterables.size(Iterables.filter(l, p));
      }
    };
  }

  public static final <T> void writeToFile(
      final File file,
      final Collection<T> collection,
      final String prefix,
      final String... suffix) throws IOException {
    final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.append(prefix);
    writer.append("size: " + collection.size());
    // collection.stream().map(T::toString).forEach(bw::append);
    for (T element : collection)
      writer.append(element.toString());
    if (suffix.length > 0)
      writer.append(suffix[0]);
    writer.close();
  }
}
