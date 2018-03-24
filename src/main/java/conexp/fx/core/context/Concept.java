/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import conexp.fx.core.math.PartialComparable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class Concept<G, M>
    implements de.tudresden.inf.tcs.fcaapi.Concept<M, G>, PartialComparable<Concept<G, M>>, Cloneable {

  private final ObservableSet<G> extent;
  private final ObservableSet<M> intent;

  public Concept(final Set<G> extent, final Set<M> intent) {
    super();
    this.extent = (extent instanceof ObservableSet) ? (ObservableSet<G>) extent : FXCollections.observableSet(extent);
    this.intent = (intent instanceof ObservableSet) ? (ObservableSet<M>) intent : FXCollections.observableSet(intent);
  }

  public Concept(final Collection<G> extent, final Collection<M> intent) {
    this(new HashSet<G>(extent), new HashSet<M>(intent));
  }

  public final ObservableSet<G> extent() {
    return extent;
  }

  @Override
  public Set<G> getExtent() {
    return extent;
  }

  public final ObservableSet<M> intent() {
    return intent;
  }

  @Override
  public Set<M> getIntent() {
    return intent;
  }

  public final boolean smallerEq(final Concept<G, M> concept) {
    return intent.containsAll(concept.intent);
  }

  public final boolean smaller(final Concept<G, M> concept) {
    return intent.size() > concept.intent.size() && smallerEq(concept);
  }

  public final boolean greaterEq(final Concept<G, M> concept) {
    return concept.smallerEq(this);
  }

  public final boolean greater(final Concept<G, M> concept) {
    return concept.smaller(this);
  }

  public final boolean uncomparable(final Concept<G, M> concept) {
    return !smallerEq(concept) && !greaterEq(concept);
  }

  public final int compareTo(final Concept<G, M> concept) {
    if (equals(concept))
      return 0;
    if (smallerEq(concept))
      return -1;
    if (greaterEq(concept))
      return 1;
    return Integer.MAX_VALUE;
  }

  public final Concept<G, M> clone() {
    return new Concept<G, M>(new HashSet<G>(extent), new HashSet<M>(intent));
  }

  public final boolean equals(final Object object) {
    return (object != null) && (object instanceof Concept) && extent.equals(((Concept<?, ?>) object).extent)
//        && intent.equals(((Concept<?, ?>) object).intent)
    ;
  }

  public final int hashCode() {
    return extent.hashCode();// + intent.hashCode();
  }

  public final String toString() {
    final StringBuilder s = new StringBuilder();
    s.append("(");
    s.append("{");
    boolean first = true;
    for (G g : extent) {
      if (first)
        first = false;
      else
        s.append(",");
      s.append(g.toString());
    }
    s.append("}");
    s.append(",");
    s.append("{");
    first = true;
    for (M m : intent) {
      if (first)
        first = false;
      else
        s.append(",");
      s.append(m.toString());
    }
    s.append("}");
    s.append(")");
    return s.toString();
  }
}
