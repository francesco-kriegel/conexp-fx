/**
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.context;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import conexp.fx.core.math.PartialComparable;

public final class Concept<G, M> implements de.tudresden.inf.tcs.fcaapi.Concept<M, G>,
		PartialComparable<Concept<G, M>>, Cloneable {

	private final Set<G> extent;
	private final ObservableSet<M> intent;

	public Concept(final Collection<G> extent, final Collection<M> intent) {
		super();
		this.extent = new HashSet<G>(extent);
		this.intent = FXCollections.observableSet(Collections.synchronizedSet(new HashSet<M>(intent)));
	}

	public final Set<G> extent() {
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
		return (object != null) && (object instanceof Concept) && extent.equals(((Concept<?, ?>) object).extent);
	}

	public final int hashCode() {
		return extent.hashCode();
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
