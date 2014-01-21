package conexp.fx.core.xml;

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


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class FloatListData extends ListData<Float> {

	public FloatListData(final String key, final String subkey,
			final List<Float> value) throws NullPointerException,
			IndexOutOfBoundsException {
		super(Datatype.FLOAT_LIST, key, subkey, value);
	}

	public FloatListData(final String key, final String subkey,
			final Float... values) throws NullPointerException,
			IndexOutOfBoundsException {
		this(key, subkey, Arrays.asList(values));
	}

	public FloatListData(final String key, final String subkey, final Void v,
			final List<String> value) throws NullPointerException,
			IndexOutOfBoundsException {
		this(key, subkey, Lists.transform(value, STRING_TO_FLOAT_FUNCTION));
	}

	public FloatListData(final String key, final String subkey, final Void v,
			final String... values) throws NullPointerException,
			IndexOutOfBoundsException {
		this(key, subkey, Lists.transform(Arrays.asList(values),
				STRING_TO_FLOAT_FUNCTION));
	}

	private static final Function<String, Float> STRING_TO_FLOAT_FUNCTION = new Function<String, Float>() {

		@Override
		public final Float apply(final String value) {
			return Float.valueOf(value);
		}
	};

	@Override
	public final boolean add(Float value) {
		return this.value.add(value);
	}

	@Override
	public final void add(int index, Float value) {
		this.value.add(index, value);
	}

	@Override
	public final Float set(int index, Float value) {
		return this.value.set(index, value);
	}

	@Override
	public final Float get(int index) {
		return value.get(index);
	}

	@Override
	public final Float remove(int index) {
		return value.remove(index);
	}

	@Override
	public final boolean remove(Object object) {
		return value.remove(object);
	}

	@Override
	public final boolean contains(Object object) {
		return this.value.contains(object);
	}

	@Override
	public final int indexOf(Object object) {
		return this.value.indexOf(object);
	}

	@Override
	public final int lastIndexOf(Object object) {
		return value.lastIndexOf(object);
	}

	@Override
	public final boolean addAll(Collection<? extends Float> collection) {
		return value.addAll(collection);
	}

	@Override
	public final boolean addAll(int index,
			Collection<? extends Float> collection) {
		return value.addAll(index, collection);
	}

	@Override
	public final boolean removeAll(Collection<?> collection) {
		return value.removeAll(collection);
	}

	@Override
	public final boolean retainAll(Collection<?> collection) {
		return value.retainAll(collection);
	}

	@Override
	public final boolean containsAll(Collection<?> collection) {
		return value.containsAll(collection);
	}

	@Override
	public final void clear() {
		value.clear();
	}

	@Override
	public final boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public final int size() {
		return value.size();
	}

	@Override
	public final Iterator<Float> iterator() {
		return value.iterator();
	}

	@Override
	public final ListIterator<Float> listIterator() {
		return value.listIterator();
	}

	@Override
	public final ListIterator<Float> listIterator(int index) {
		return value.listIterator(index);
	}

	@Override
	public final List<Float> subList(int fromIndex, int toIndex) {
		return value.subList(fromIndex, toIndex);
	}

	@Override
	public final Object[] toArray() {
		return value.toArray();
	}

	@Override
	public final <T> T[] toArray(T[] array) {
		return value.<T> toArray(array);
	}

}