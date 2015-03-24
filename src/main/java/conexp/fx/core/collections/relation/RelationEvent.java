package conexp.fx.core.collections.relation;

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


import java.util.Collections;
import java.util.Set;

import conexp.fx.core.collections.pair.Pair;

public final class RelationEvent<R, C> {

  public enum Type {
    ANY(null),
    ROWS(ANY),
    ROWS_ADDED(ROWS),
    ROWS_SET(ROWS),
    ROWS_REMOVED(ROWS),
    ROWS_CLEARED(ROWS),
    COLUMNS(ANY),
    COLUMNS_ADDED(COLUMNS),
    COLUMNS_SET(COLUMNS),
    COLUMNS_REMOVED(COLUMNS),
    COLUMNS_CLEARED(COLUMNS),
    ENTRIES(ANY),
    ENTRIES_ADDED(ENTRIES),
    ENTRIES_REMOVED(ENTRIES),
    ALL_CHANGED(ENTRIES),
    SELECTION_CHANGED(ANY);

    private final Type superType;

    Type(final Type superType) {
      this.superType = superType;
    }

    public final Type getSuperType() {
      return superType;
    }
  }

  public static final Type      ANY               = Type.ANY;
  public static final Type      ROWS              = Type.ROWS;
  public static final Type      ROWS_ADDED        = Type.ROWS_ADDED;
  public static final Type      ROWS_SET          = Type.COLUMNS_SET;
  public static final Type      ROWS_REMOVED      = Type.ROWS_REMOVED;
  public static final Type      ROWS_CLEARED      = Type.ROWS_CLEARED;
  public static final Type      COLUMNS           = Type.COLUMNS;
  public static final Type      COLUMNS_ADDED     = Type.COLUMNS_ADDED;
  public static final Type      COLUMNS_SET       = Type.COLUMNS_SET;
  public static final Type      COLUMNS_REMOVED   = Type.COLUMNS_REMOVED;
  public static final Type      COLUMNS_CLEARED   = Type.COLUMNS_CLEARED;
  public static final Type      ENTRIES           = Type.ENTRIES;
  public static final Type      ENTRIES_ADDED     = Type.ENTRIES_ADDED;
  public static final Type      ENTRIES_REMOVED   = Type.ENTRIES_REMOVED;
  public static final Type      ALL_CHANGED       = Type.ALL_CHANGED;
  public static final Type      SELECTION_CHANGED = Type.SELECTION_CHANGED;
  private final Type            type;
  private final Set<R>          rows;
  private final Set<Pair<R, R>> setRows;
  private final Set<C>          cols;
  private final Set<Pair<C, C>> setCols;
  private final Set<Pair<R, C>> entries;

  public RelationEvent(final Type type) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(Collections.<R> emptySet());
    this.setRows = Collections.unmodifiableSet(Collections.<Pair<R, R>> emptySet());
    this.cols = Collections.unmodifiableSet(Collections.<C> emptySet());
    this.setCols = Collections.unmodifiableSet(Collections.<Pair<C, C>> emptySet());
    this.entries = Collections.unmodifiableSet(Collections.<Pair<R, C>> emptySet());
  }

  public RelationEvent(final Type type, final R row, final C col) {
    this(type, row == null ? null : Collections.singleton(row), col == null ? null : Collections.singleton(col), null);
  }

  public RelationEvent(final Type type, final Set<R> rows, final Set<C> columns, final Set<Pair<R, C>> entries) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(rows == null ? Collections.<R> emptySet() : rows);
    this.setRows = Collections.unmodifiableSet(Collections.<Pair<R, R>> emptySet());
    this.cols = Collections.unmodifiableSet(columns == null ? Collections.<C> emptySet() : columns);
    this.setCols = Collections.unmodifiableSet(Collections.<Pair<C, C>> emptySet());
    this.entries = Collections.unmodifiableSet(entries == null ? Collections.<Pair<R, C>> emptySet() : entries);
  }

  public RelationEvent(final Type type, final Pair<R, R> setRow, final Pair<C, C> setColumn) {
    this(type, setRow == null ? null : Collections.singleton(setRow), setColumn == null ? null : Collections
        .singleton(setColumn));
  }

  public RelationEvent(final Type type, final Set<Pair<R, R>> setRows, final Set<Pair<C, C>> setColumns) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(Collections.<R> emptySet());
    this.setRows = Collections.unmodifiableSet(setRows == null ? Collections.<Pair<R, R>> emptySet() : setRows);
    this.cols = Collections.unmodifiableSet(Collections.<C> emptySet());
    this.setCols = Collections.unmodifiableSet(setColumns == null ? Collections.<Pair<C, C>> emptySet() : setColumns);
    this.entries = Collections.unmodifiableSet(Collections.<Pair<R, C>> emptySet());
  }

  public final Type getType() {
    return type;
  }

  public final Set<R> getRows() {
    return rows;
  }

  public final Set<Pair<R, R>> getSetRows() {
    return setRows;
  }

  public final Set<C> getColumns() {
    return cols;
  }

  public final Set<Pair<C, C>> getSetColumns() {
    return setCols;
  }

  public final Set<Pair<R, C>> getEntries() {
    return entries;
  }
}
