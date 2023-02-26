package conexp.fx.core.collections.relation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import java.util.Collections;
import java.util.Set;

import conexp.fx.core.collections.Pair;

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

  public static final RelationEvent.Type ANY               = RelationEvent.Type.ANY;
  public static final RelationEvent.Type ROWS              = RelationEvent.Type.ROWS;
  public static final RelationEvent.Type ROWS_ADDED        = RelationEvent.Type.ROWS_ADDED;
  public static final RelationEvent.Type ROWS_SET          = RelationEvent.Type.ROWS_SET;
  public static final RelationEvent.Type ROWS_REMOVED      = RelationEvent.Type.ROWS_REMOVED;
  public static final RelationEvent.Type ROWS_CLEARED      = RelationEvent.Type.ROWS_CLEARED;
  public static final RelationEvent.Type COLUMNS           = RelationEvent.Type.COLUMNS;
  public static final RelationEvent.Type COLUMNS_ADDED     = RelationEvent.Type.COLUMNS_ADDED;
  public static final RelationEvent.Type COLUMNS_SET       = RelationEvent.Type.COLUMNS_SET;
  public static final RelationEvent.Type COLUMNS_REMOVED   = RelationEvent.Type.COLUMNS_REMOVED;
  public static final RelationEvent.Type COLUMNS_CLEARED   = RelationEvent.Type.COLUMNS_CLEARED;
  public static final RelationEvent.Type ENTRIES           = RelationEvent.Type.ENTRIES;
  public static final RelationEvent.Type ENTRIES_ADDED     = RelationEvent.Type.ENTRIES_ADDED;
  public static final RelationEvent.Type ENTRIES_REMOVED   = RelationEvent.Type.ENTRIES_REMOVED;
  public static final RelationEvent.Type ALL_CHANGED       = RelationEvent.Type.ALL_CHANGED;
  public static final RelationEvent.Type SELECTION_CHANGED = RelationEvent.Type.SELECTION_CHANGED;

  private final RelationEvent.Type       type;
  private final Set<R>                   rows;
  private final Set<Pair<R, R>>          setRows;
  private final Set<C>                   cols;
  private final Set<Pair<C, C>>          setCols;
  private final Set<Pair<R, C>>          entries;

  public RelationEvent(final RelationEvent.Type type) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(Collections.<R> emptySet());
    this.setRows = Collections.unmodifiableSet(Collections.<Pair<R, R>> emptySet());
    this.cols = Collections.unmodifiableSet(Collections.<C> emptySet());
    this.setCols = Collections.unmodifiableSet(Collections.<Pair<C, C>> emptySet());
    this.entries = Collections.unmodifiableSet(Collections.<Pair<R, C>> emptySet());
  }

  public RelationEvent(final RelationEvent.Type type, final R row, final C col) {
    this(type, row == null ? null : Collections.singleton(row), col == null ? null : Collections.singleton(col), null);
  }

  public RelationEvent(
      final RelationEvent.Type type,
      final Set<R> rows,
      final Set<C> columns,
      final Set<Pair<R, C>> entries) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(rows == null ? Collections.<R> emptySet() : rows);
    this.setRows = Collections.unmodifiableSet(Collections.<Pair<R, R>> emptySet());
    this.cols = Collections.unmodifiableSet(columns == null ? Collections.<C> emptySet() : columns);
    this.setCols = Collections.unmodifiableSet(Collections.<Pair<C, C>> emptySet());
    this.entries = Collections.unmodifiableSet(entries == null ? Collections.<Pair<R, C>> emptySet() : entries);
  }

  public RelationEvent(final RelationEvent.Type type, final Pair<R, R> setRow, final Pair<C, C> setColumn) {
    this(
        type,
        setRow == null ? null : Collections.singleton(setRow),
        setColumn == null ? null : Collections.singleton(setColumn));
  }

  public RelationEvent(final RelationEvent.Type type, final Set<Pair<R, R>> setRows, final Set<Pair<C, C>> setColumns) {
    super();
    this.type = type;
    this.rows = Collections.unmodifiableSet(Collections.<R> emptySet());
    this.setRows = Collections.unmodifiableSet(setRows == null ? Collections.<Pair<R, R>> emptySet() : setRows);
    this.cols = Collections.unmodifiableSet(Collections.<C> emptySet());
    this.setCols = Collections.unmodifiableSet(setColumns == null ? Collections.<Pair<C, C>> emptySet() : setColumns);
    this.entries = Collections.unmodifiableSet(Collections.<Pair<R, C>> emptySet());
  }

  public final RelationEvent.Type getType() {
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
