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


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.ujmp.core.Matrix;
import org.ujmp.core.booleanmatrix.BooleanMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.collections.Collections3;
import conexp.fx.core.collections.IterableSet;
import conexp.fx.core.collections.ListIterators;
import conexp.fx.core.collections.pair.Pair;
import conexp.fx.core.collections.relation.AbstractRelation;
import conexp.fx.core.collections.relation.MatrixRelation;
import conexp.fx.core.collections.relation.Relation;
import conexp.fx.core.collections.relation.RelationEvent;
import conexp.fx.core.collections.relation.RelationEventHandler;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.math.BooleanMatrices;
import conexp.fx.core.math.Isomorphism;
import conexp.fx.core.util.Constants;

public class MatrixContext<G, M>
  extends MatrixRelation<G, M>
  implements Context<G, M>
{
  public enum Incidence
  {
    CROSS(Constants.CROSS_CHARACTER),
    DOWN_ARROW(Constants.DOWN_ARROW_CHARACTER),
    UP_ARROW(Constants.UP_ARROW_CHARACTER),
    BOTH_ARROW(Constants.BOTH_ARROW_CHARACTER),
    DOWN_PATH(Constants.DOUBLE_DOWN_ARROW_CHARACTER),
    UP_PATH(Constants.DOUBLE_UP_ARROW_CHARACTER),
    BOTH_PATH(Constants.DOUBLE_BOTH_ARROW_CHARACTER),
    NO_CROSS(Constants.NO_CROSS_CHARACTER);
    private final String character;

    private Incidence(final String character)
    {
      this.character = character;
    }

    public final String toString()
    {
      return character;
    }
  }

  public final StringProperty                      id                      = new SimpleStringProperty(null);
  public MatrixContext<G, M>                       selection               = this;
  public final SetList<Set<Integer>>               _objects                = new HashSetArrayList<Set<Integer>>();
  public final SetList<Set<Integer>>               _attributes             = new HashSetArrayList<Set<Integer>>();
  private final Collection<Integer>                i                       = Collections2.transform(
                                                                               _objects,
                                                                               Collections3.<Integer> firstElement());
  private final Collection<Integer>                j                       = Collections2.transform(
                                                                               _attributes,
                                                                               Collections3.<Integer> firstElement());
  public final Context<Set<Integer>, Set<Integer>> _cleaned                =
                                                                               new AbstractContext<Set<Integer>, Set<Integer>>(
                                                                                   _objects,
                                                                                   _attributes,
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public final boolean contains(
                                                                                       final Object _i,
                                                                                       final Object _j)
                                                                                   {
                                                                                     return matrix
                                                                                         .getBoolean(
                                                                                             Collections3
                                                                                                 .firstElement((Set<Integer>) _i),
                                                                                             Collections3
                                                                                                 .firstElement((Set<Integer>) _j));
                                                                                   }

                                                                                   public final
                                                                                       MatrixContext<Set<Integer>, Set<Integer>>
                                                                                       clone()
                                                                                   {
                                                                                     return new MatrixContext<Set<Integer>, Set<Integer>>(
                                                                                         _objects,
                                                                                         _attributes,
                                                                                         (BooleanMatrix) matrix
                                                                                             .selectRows(Ret.NEW, i)
                                                                                             .selectColumns(Ret.NEW, j),
                                                                                         false);
                                                                                   }
                                                                                 };
  public Relation<Set<Integer>, Set<Integer>>      _downArrows             =
                                                                               new AbstractRelation<Set<Integer>, Set<Integer>>(
                                                                                   _objects,
                                                                                   _attributes,
                                                                                   false)
                                                                                 {
                                                                                   public final boolean contains(
                                                                                       final Object object,
                                                                                       final Object attribute)
                                                                                   {
                                                                                     @SuppressWarnings("unchecked")
                                                                                     final int rowIndex =
                                                                                         Collections3
                                                                                             .firstElement((Set<Integer>) object);
                                                                                     @SuppressWarnings("unchecked")
                                                                                     final int columnIndex =
                                                                                         Collections3
                                                                                             .firstElement((Set<Integer>) attribute);
                                                                                     if (!matrix.getBoolean(
                                                                                         rowIndex,
                                                                                         columnIndex))
                                                                                       return _col(columnIndex, i)
                                                                                           .containsAll(
                                                                                               _extent(
                                                                                                   Collections
                                                                                                       .singleton(rowIndex),
                                                                                                   Collections2
                                                                                                       .filter(
                                                                                                           i,
                                                                                                           Predicates
                                                                                                               .not(Predicates
                                                                                                                   .equalTo(rowIndex))),
                                                                                                   j));
                                                                                     return false;
                                                                                   }
                                                                                 };
  public Relation<Set<Integer>, Set<Integer>>      _upArrows               =
                                                                               new AbstractRelation<Set<Integer>, Set<Integer>>(
                                                                                   _objects,
                                                                                   _attributes,
                                                                                   false)
                                                                                 {
                                                                                   public final boolean contains(
                                                                                       final Object object,
                                                                                       final Object attribute)
                                                                                   {
                                                                                     @SuppressWarnings("unchecked")
                                                                                     final int _i =
                                                                                         Collections3
                                                                                             .firstElement((Set<Integer>) object);
                                                                                     @SuppressWarnings("unchecked")
                                                                                     final int _j =
                                                                                         Collections3
                                                                                             .firstElement((Set<Integer>) attribute);
                                                                                     if (!matrix.getBoolean(_i, _j))
                                                                                       return _row(_i, j)
                                                                                           .containsAll(
                                                                                               _intent(
                                                                                                   Collections
                                                                                                       .singleton(_j),
                                                                                                   i,
                                                                                                   Collections2
                                                                                                       .filter(
                                                                                                           j,
                                                                                                           Predicates
                                                                                                               .not(Predicates
                                                                                                                   .equalTo(_j)))));
                                                                                     return false;
                                                                                   }
                                                                                 };                                    ;
  public Relation<Set<Integer>, Set<Integer>>      _downPaths;
  public Relation<Set<Integer>, Set<Integer>>      _upPaths;
  private final Predicate<Set<Integer>>            _isIrreducibleObject    = new Predicate<Set<Integer>>()
                                                                             {
                                                                               public final boolean apply(
                                                                                   final Set<Integer> i)
                                                                               {
                                                                                 for (Set<Integer> j : _attributes)
                                                                                   if (_downArrows.contains(i, j))
                                                                                     return true;
                                                                                 return false;
                                                                               }
                                                                             };
  private final Predicate<Set<Integer>>            _isIrreducibleAttribute = new Predicate<Set<Integer>>()
                                                                             {
                                                                               public final boolean apply(
                                                                                   final Set<Integer> j)
                                                                               {
                                                                                 for (Set<Integer> i : _objects)
                                                                                   if (_upArrows.contains(i, j))
                                                                                     return true;
                                                                                 return false;
                                                                               }
                                                                             };
  public final SetList<Set<Integer>>               _irreducibleObjects     = new HashSetArrayList<Set<Integer>>();
  public final SetList<Set<Integer>>               _irreducibleAttributes  = new HashSetArrayList<Set<Integer>>();
  public final Context<Set<Integer>, Set<Integer>> _reduced                =
                                                                               new AbstractContext<Set<Integer>, Set<Integer>>(
                                                                                   _irreducibleObjects,
                                                                                   _irreducibleAttributes,
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public final boolean contains(
                                                                                       final Object _i,
                                                                                       final Object _j)
                                                                                   {
                                                                                     return matrix
                                                                                         .getBoolean(
                                                                                             Collections3
                                                                                                 .firstElement((Set<Integer>) _i),
                                                                                             Collections3
                                                                                                 .firstElement((Set<Integer>) _j));
                                                                                   }

                                                                                   public final
                                                                                       MatrixContext<Set<Integer>, Set<Integer>>
                                                                                       clone()
                                                                                   {
                                                                                     return new MatrixContext<Set<Integer>, Set<Integer>>(
                                                                                         _irreducibleObjects,
                                                                                         _irreducibleAttributes,
                                                                                         (BooleanMatrix) matrix
                                                                                             .selectRows(
                                                                                                 Ret.NEW,
                                                                                                 Collections2
                                                                                                     .transform(
                                                                                                         _irreducibleObjects,
                                                                                                         Collections3
                                                                                                             .<Integer> firstElement()))
                                                                                             .selectColumns(
                                                                                                 Ret.NEW,
                                                                                                 Collections2
                                                                                                     .transform(
                                                                                                         _irreducibleAttributes,
                                                                                                         Collections3
                                                                                                             .<Integer> firstElement())),
                                                                                         false);
                                                                                   }
                                                                                 };                                    ;
  public final Function<Iterable<Integer>, G>      _firstObject            = Functions.compose(
                                                                               rowHeads.index().inverse(),
                                                                               Collections3.<Integer> firstElement());
  public final Function<Iterable<Integer>, M>      _firstAttribute         = Functions.compose(
                                                                               colHeads.index().inverse(),
                                                                               Collections3.<Integer> firstElement());
  public final Isomorphism<Set<Integer>, Set<G>>   _allObjects             = new Isomorphism<Set<Integer>, Set<G>>()
                                                                             {
                                                                               public final Set<G> apply(
                                                                                   final Set<Integer> set)
                                                                               {
                                                                                 return Collections3.transform(
                                                                                     set,
                                                                                     Isomorphism.invert(rowHeads()
                                                                                         .index()));
                                                                               }

                                                                               public Set<Integer> invert(
                                                                                   final Set<G> set)
                                                                               {
                                                                                 return Collections3.transform(
                                                                                     set,
                                                                                     rowHeads().index());
                                                                               }
                                                                             };
  public final Isomorphism<Set<Integer>, Set<M>>   _allAttributes          = new Isomorphism<Set<Integer>, Set<M>>()
                                                                             {
                                                                               public final Set<M> apply(
                                                                                   final Set<Integer> set)
                                                                               {
                                                                                 return Collections3.transform(
                                                                                     set,
                                                                                     Isomorphism.invert(colHeads()
                                                                                         .index()));
                                                                               }

                                                                               public Set<Integer> invert(
                                                                                   final Set<M> set)
                                                                               {
                                                                                 return Collections3.transform(
                                                                                     set,
                                                                                     colHeads().index());
                                                                               }
                                                                             };
  public final AbstractContext<Set<G>, Set<M>>     cleaned                 =
                                                                               new AbstractContext<Set<G>, Set<M>>(
                                                                                   SetLists.transform(
                                                                                       _objects,
                                                                                       _allObjects),
                                                                                   SetLists.transform(
                                                                                       _attributes,
                                                                                       _allAttributes),
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public boolean contains(
                                                                                       Object o1,
                                                                                       Object o2)
                                                                                   {
                                                                                     return _cleaned.contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                                   }
                                                                                 };
  public final AbstractContext<Set<G>, Set<M>>     reduced                 = new AbstractContext<Set<G>, Set<M>>(
                                                                               SetLists.transform(
                                                                                   _irreducibleObjects,
                                                                                   _allObjects), SetLists.transform(
                                                                                   _irreducibleAttributes,
                                                                                   _allAttributes), false)
                                                                             {
                                                                               @SuppressWarnings("unchecked")
                                                                               public boolean contains(
                                                                                   Object o1,
                                                                                   Object o2)
                                                                               {
                                                                                 return _reduced
                                                                                     .contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                               }
                                                                             };
  public final AbstractRelation<Set<G>, Set<M>>    downArrows              =
                                                                               new AbstractRelation<Set<G>, Set<M>>(
                                                                                   SetLists.transform(
                                                                                       _objects,
                                                                                       _allObjects),
                                                                                   SetLists.transform(
                                                                                       _attributes,
                                                                                       _allAttributes),
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public boolean contains(
                                                                                       Object o1,
                                                                                       Object o2)
                                                                                   {
                                                                                     return _downArrows.contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                                   }
                                                                                 };
  public final AbstractRelation<Set<G>, Set<M>>    upArrows                =
                                                                               new AbstractRelation<Set<G>, Set<M>>(
                                                                                   SetLists.transform(
                                                                                       _objects,
                                                                                       _allObjects),
                                                                                   SetLists.transform(
                                                                                       _attributes,
                                                                                       _allAttributes),
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public boolean contains(
                                                                                       Object o1,
                                                                                       Object o2)
                                                                                   {
                                                                                     return _upArrows.contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                                   }
                                                                                 };
  public final AbstractRelation<G, M>              DownArrows              = new AbstractRelation<G, M>(
                                                                               rowHeads,
                                                                               colHeads,
                                                                               false)
                                                                             {
                                                                               public boolean contains(
                                                                                   Object o1,
                                                                                   Object o2)
                                                                               {
                                                                                 return _downArrows.contains(
                                                                                     _objectEquivalence().col(
                                                                                         rowHeads().indexOf(o1)),
                                                                                     _attributeEquivalence().col(
                                                                                         colHeads().indexOf(o2)));
                                                                               }
                                                                             };
  public final AbstractRelation<G, M>              UpArrows                = new AbstractRelation<G, M>(
                                                                               rowHeads,
                                                                               colHeads,
                                                                               false)
                                                                             {
                                                                               public boolean contains(
                                                                                   Object o1,
                                                                                   Object o2)
                                                                               {
                                                                                 return _upArrows.contains(
                                                                                     _objectEquivalence().col(
                                                                                         rowHeads().indexOf(o1)),
                                                                                     _attributeEquivalence().col(
                                                                                         colHeads().indexOf(o2)));
                                                                               }
                                                                             };
  public final AbstractRelation<Set<G>, Set<M>>    downPaths               =
                                                                               new AbstractRelation<Set<G>, Set<M>>(
                                                                                   SetLists.transform(
                                                                                       _objects,
                                                                                       _allObjects),
                                                                                   SetLists.transform(
                                                                                       _attributes,
                                                                                       _allAttributes),
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public boolean contains(
                                                                                       Object o1,
                                                                                       Object o2)
                                                                                   {
                                                                                     return _downPaths.contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                                   }
                                                                                 };
  public final AbstractRelation<Set<G>, Set<M>>    upPaths                 =
                                                                               new AbstractRelation<Set<G>, Set<M>>(
                                                                                   SetLists.transform(
                                                                                       _objects,
                                                                                       _allObjects),
                                                                                   SetLists.transform(
                                                                                       _attributes,
                                                                                       _allAttributes),
                                                                                   false)
                                                                                 {
                                                                                   @SuppressWarnings("unchecked")
                                                                                   public boolean contains(
                                                                                       Object o1,
                                                                                       Object o2)
                                                                                   {
                                                                                     return _upPaths.contains(
                                                                                         _allObjects
                                                                                             .invert((Set<G>) o1),
                                                                                         _allAttributes
                                                                                             .invert((Set<M>) o2));
                                                                                   }
                                                                                 };
  public final AbstractRelation<G, M>              DownPaths               = new AbstractRelation<G, M>(
                                                                               rowHeads,
                                                                               colHeads,
                                                                               false)
                                                                             {
                                                                               public boolean contains(
                                                                                   Object o1,
                                                                                   Object o2)
                                                                               {
                                                                                 return _downPaths.contains(
                                                                                     _objectEquivalence().col(
                                                                                         rowHeads().indexOf(o1)),
                                                                                     _attributeEquivalence().col(
                                                                                         colHeads().indexOf(o2)));
                                                                               }
                                                                             };
  public final AbstractRelation<G, M>              UpPaths                 = new AbstractRelation<G, M>(
                                                                               rowHeads,
                                                                               colHeads,
                                                                               false)
                                                                             {
                                                                               public boolean contains(
                                                                                   Object o1,
                                                                                   Object o2)
                                                                               {
                                                                                 return _upPaths.contains(
                                                                                     _objectEquivalence().col(
                                                                                         rowHeads().indexOf(o1)),
                                                                                     _attributeEquivalence().col(
                                                                                         colHeads().indexOf(o2)));
                                                                               }
                                                                             };
  private final Set<M>                             ignoredAttributes       = new HashSet<M>();

  public MatrixContext(final boolean homogen)
  {
    super(homogen);
    initHandlers(true, true);
  }

  public MatrixContext(final SetList<G> objects, final SetList<M> attributes, final boolean homogen)
  {
    super(objects, attributes, homogen);
    initHandlers(true, true);
  }

  public MatrixContext(
      final SetList<G> objects,
      final SetList<M> attributes,
      final BooleanMatrix matrix,
      final boolean homogen)
  {
    super(objects, attributes, matrix, homogen);
    initHandlers(true, true);
  }

  public MatrixContext(final boolean homogen, final boolean selfReducing)
  {
    super(homogen);
    initHandlers(true, selfReducing);
  }

  public MatrixContext(final SetList<G> objects, final SetList<M> attributes, final boolean homogen, final boolean selfReducing)
  {
    super(objects, attributes, homogen);
    initHandlers(true, selfReducing);
  }

  public MatrixContext(
      final SetList<G> objects,
      final SetList<M> attributes,
      final BooleanMatrix matrix,
      final boolean homogen, final boolean selfReducing)
  {
    super(objects, attributes, matrix, homogen);
    initHandlers(true, selfReducing);
  }

  private boolean lock = false;

  public final void lock()
  {
    lock = true;
  }

  public final void unlock()
  {
    lock = false;
  }

  private void initHandlers(final boolean selfSelecting, final boolean selfReducing)
  {
//    if (selfSelecting) {
//      addEventHandler(new RelationEventHandler<G, M>() {
//
//        
//        public final void handle(final RelationEvent<G, M> event) {
//          select();
//        }
//      }, RelationEvent.SELECTION_CHANGED);
//      select();
//    }
    if (selfReducing) {
      addEventHandler(new RelationEventHandler<G, M>()
        {
          public final void handle(final RelationEvent<G, M> event)
          {
            if (!lock) {
//              final long start = System.currentTimeMillis();
              // System.out.println("reducing...");
              reduce();
//              System.out.println("reducing done in " + (System.currentTimeMillis() - start) + " ms.");
            }
          }
        }, RelationEvent.ALL_CHANGED, RelationEvent.ENTRIES);
      reduce();
    }
  }

  public synchronized final void select()
  {
    final SetList<M> selectedColHeads = SetLists.difference(colHeads, ignoredAttributes).clone();
    if (ignoredAttributes.isEmpty())
      selection = this;
    else if (selectedColHeads.isEmpty())
      selection = new MatrixContext<G, M>(false);
    else
      selection =
          new MatrixContext<G, M>(rowHeads, selectedColHeads, (BooleanMatrix) matrix.selectColumns(
              Ret.NEW,
              MatrixContext.this.colHeads().indicesOf(selectedColHeads, false)), false);
  }

  public synchronized final void reduce()
  {
    _objects.clear();
    _objects.addAll(_objectEquivalence().equivalenceClasses());
    _attributes.clear();
    _attributes.addAll(_attributeEquivalence().equivalenceClasses());
    _downArrows = new AbstractRelation<Set<Integer>, Set<Integer>>(_objects, _attributes, false)
      {
        public final boolean contains(final Object object, final Object attribute)
        {
          @SuppressWarnings("unchecked")
          final int rowIndex = Collections3.firstElement((Set<Integer>) object);
          @SuppressWarnings("unchecked")
          final int columnIndex = Collections3.firstElement((Set<Integer>) attribute);
          if (!matrix.getBoolean(rowIndex, columnIndex))
            return _col(columnIndex, i).containsAll(
                _extent(
                    Collections.singleton(rowIndex),
                    Collections2.filter(i, Predicates.not(Predicates.equalTo(rowIndex))),
                    j));
          return false;
        }
      }.clone();
    _upArrows = new AbstractRelation<Set<Integer>, Set<Integer>>(_objects, _attributes, false)
      {
        public final boolean contains(final Object object, final Object attribute)
        {
          @SuppressWarnings("unchecked")
          final int _i = Collections3.firstElement((Set<Integer>) object);
          @SuppressWarnings("unchecked")
          final int _j = Collections3.firstElement((Set<Integer>) attribute);
          if (!matrix.getBoolean(_i, _j))
            return _row(_i, j).containsAll(
                _intent(Collections.singleton(_j), i, Collections2.filter(j, Predicates.not(Predicates.equalTo(_j)))));
          return false;
        }
      }.clone();
    _irreducibleObjects.clear();
    _irreducibleObjects.addAll(_objects.filter(_isIrreducibleObject));
    _irreducibleAttributes.clear();
    _irreducibleAttributes.addAll(_attributes.filter(_isIrreducibleAttribute).clone());
//    try {
//      final int rows = _objects.size();
//      final int cols = _attributes.size();
//      @SuppressWarnings("deprecation")
//      final BooleanMatrix arrowPaths =
//          BooleanMatrices.transitiveClosure(BooleanMatrices.reflexiveClosure(BooleanMatrices.quadPosition(
//              BooleanMatrices.empty(rows),
//              _downArrows.clone().matrix(),
//              BooleanMatrices.dual(_upArrows.clone().matrix()),
//              BooleanMatrices.empty(cols))));
//      final BooleanMatrix downPaths = (BooleanMatrix) arrowPaths.subMatrix(Ret.NEW, 0, rows, rows - 1, rows + cols - 1);
//      final BooleanMatrix upPaths =
//          (BooleanMatrix) BooleanMatrices.dual((BooleanMatrix) arrowPaths.subMatrix(
//              Ret.NEW,
//              rows,
//              0,
//              rows + cols - 1,
//              rows - 1));
//      _downPaths = new MatrixRelation<Set<Integer>, Set<Integer>>(_objects, _attributes, downPaths, false);
//      _upPaths = new MatrixRelation<Set<Integer>, Set<Integer>>(_objects, _attributes, upPaths, false);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  public final Pair<Incidence, Incidence> getValue(final G g, final M m, final boolean... withArrows)
  {
    if (withArrows.length == 0 || withArrows[0]) {
      Incidence first;
      Incidence second;
      boolean down = false;
      boolean up = false;
      if (contains(g, m))
        first = Incidence.CROSS;
      else {
        down = DownArrows.contains(g, m);
        up = UpArrows.contains(g, m);
        if (down && up)
          first = Incidence.BOTH_ARROW;
        else if (down)
          first = Incidence.DOWN_ARROW;
        else if (up)
          first = Incidence.UP_ARROW;
        else
          first = Incidence.NO_CROSS;
      }
//      final boolean Down = DownPaths.contains(g, m);
//      final boolean Up = UpPaths.contains(g, m);
//      if (up || down)
//        second = null;
//      else if (Down && Up)
//        second = Incidence.BOTH_PATH;
//      else if (Down)
//        second = Incidence.DOWN_PATH;
//      else if (Up)
//        second = Incidence.UP_PATH;
//      else
      second = null;
      return Pair.of(first, second);
    } else {
      if (contains(g, m))
        return Pair.of(Incidence.CROSS, null);
      else
        return Pair.of(Incidence.NO_CROSS, null);
    }
  }

  public final Iterable<Concept<G, M>> concepts()
  {
    return new NextConcept<G, M>(this);
  }

  public final Relation<G, G> objectQuasiOrder()
  {
    return new AbstractRelation<G, G>(rowHeads(), rowHeads(), true)
      {
        public final boolean contains(final Object object1, final Object object2)
        {
          return !matrix
              .selectRows(Ret.LINK, rowHeads().indexOf(object1))
              .ge(Ret.LINK, matrix.selectRows(Ret.LINK, rowHeads().indexOf(object2)))
              .containsBoolean(false);
        }
      };
  }

  public final Relation<M, M> attributeQuasiOrder()
  {
    return new AbstractRelation<M, M>(colHeads(), colHeads(), true)
      {
        public final boolean contains(final Object attribute1, final Object attribute2)
        {
          return !matrix
              .selectColumns(Ret.LINK, colHeads().indexOf(attribute1))
              .ge(Ret.LINK, matrix.selectColumns(Ret.LINK, colHeads().indexOf(attribute2)))
              .containsBoolean(false);
        }
      };
  }

  public final Relation<Integer, Integer> _objectEquivalence()
  {
    return new AbstractRelation<Integer, Integer>(
        SetLists.integers(MatrixContext.this.rowHeads().size()),
        SetLists.integers(MatrixContext.this.rowHeads().size()),
        true)
      {
        public final boolean contains(final Object i1, final Object i2)
        {
          return matrix.selectRows(Ret.LINK, (Integer) i1).equals(matrix.selectRows(Ret.LINK, (Integer) i2));
        }

        public final Set<Integer> col(final Object i1)
        {
          final Matrix row = matrix.selectRows(Ret.LINK, (Integer) i1);
          return new IterableSet<Integer>()
            {
              public final Iterator<Integer> iterator()
              {
                return Iterators.filter(
                    ListIterators.integers(0, MatrixContext.this.rowHeads.size()),
                    new Predicate<Integer>()
                      {
                        public final boolean apply(final Integer i2)
                        {
                          return row.equals(matrix.selectRows(Ret.LINK, i2));
                        }
                      });
              }
            };
        }
      };
  }

  public final Relation<Integer, Integer> _attributeEquivalence()
  {
    return new AbstractRelation<Integer, Integer>(
        SetLists.integers(MatrixContext.this.colHeads().size()),
        SetLists.integers(MatrixContext.this.colHeads().size()),
        true)
      {
        public final boolean contains(final Object j1, final Object j2)
        {
          return matrix.selectColumns(Ret.LINK, (Integer) j1).equals(matrix.selectColumns(Ret.LINK, (Integer) j2));
        }

        public final Set<Integer> col(final Object j1)
        {
          final Matrix col = matrix.selectColumns(Ret.LINK, (Integer) j1);
          return new IterableSet<Integer>()
            {
              public final Iterator<Integer> iterator()
              {
                return Iterators.filter(
                    ListIterators.integers(0, MatrixContext.this.colHeads.size()),
                    new Predicate<Integer>()
                      {
                        public final boolean apply(final Integer j2)
                        {
                          return col.equals(matrix.selectColumns(Ret.LINK, j2));
                        }
                      });
              }
            };
        }
      };
  }

  public final Set<M> infimumIrreducibles()
  {
    return new HashSet<M>(SetLists.transform(_irreducibleAttributes, _firstAttribute));
  }

  public final Set<G> supremumIrreducibles()
  {
    return new HashSet<G>(SetLists.transform(_irreducibleObjects, _firstObject));
  }

//  public final boolean downArrow(final Object object, final Object attribute) {
//    return _downArrows.contains(
//        _objectEquivalence().col(rowHeads().indexOf(object)),
//        _attributeEquivalence().col(colHeads().indexOf(attribute)));
//  }
//
//  public final boolean upArrow(final Object object, final Object attribute) {
//    return _upArrows.contains(
//        _objectEquivalence().col(rowHeads().indexOf(object)),
//        _attributeEquivalence().col(colHeads().indexOf(attribute)));
//  }
//  public final boolean _downArrow(final int rowIndex, final int columnIndex) {
//    return _downArrows.contains(_objectEquivalence().col(rowIndex), _attributeEquivalence().col(columnIndex));
//  }
//
//  public final boolean _upArrow(final int rowIndex, final int columnIndex) {
//    return _upArrows.contains(_objectEquivalence().col(rowIndex), _attributeEquivalence().col(columnIndex));
//  }
  public final Set<G> extent(final Object... objects)
  {
    return extent(Arrays.asList(objects));
  }

  public final Set<M> intent(final Object... attributes)
  {
    return intent(Arrays.asList(attributes));
  }

  public final Set<G> extent(final Collection<?> objects)
  {
    return new IterableSet<G>()
      {
        public Iterator<G> iterator()
        {
          return rowHeads.getAll(_extent(rowHeads.indicesOf(objects, true)), true).iterator();
        }
      };
  }

  public final Set<M> intent(final Collection<?> attributes)
  {
    return new IterableSet<M>()
      {
        public final Iterator<M> iterator()
        {
          return colHeads.getAll(_intent(colHeads.indicesOf(attributes, true)), true).iterator();
        }
      };
  }

  public final Collection<Integer> _extent(final int... rowIndices)
  {
    return _extent(Ints.asList(rowIndices));
  }

  public final Collection<Integer> _intent(final int... columnIndices)
  {
    return _intent(Ints.asList(columnIndices));
  }

  public final Collection<Integer> _extent(final Iterable<Integer> rowIndices)
  {
    return _colAnd(_rowAnd(rowIndices));
  }

  public final Collection<Integer> _intent(final Iterable<Integer> columnIndices)
  {
    return _rowAnd(_colAnd(columnIndices));
  }

  public final Collection<Integer> _extent(
      final Collection<Integer> rowIndices,
      final Collection<Integer> subRowIndices,
      final Collection<Integer> subColumnIndices)
  {
    return _colAnd(_rowAnd(rowIndices, subColumnIndices), subRowIndices);
  }

  public final Collection<Integer> _intent(
      final Collection<Integer> columnIndices,
      final Collection<Integer> subRowIndices,
      final Collection<Integer> subColumnIndices)
  {
    return _rowAnd(_colAnd(columnIndices, subRowIndices), subColumnIndices);
  }

  public final Concept<G, M> topConcept()
  {
    final Set<G> extent = rowHeads().clone();
    return new Concept<G, M>(extent, rowAnd(extent));
  }

  public final Concept<G, M> bottomConcept()
  {
    final Set<M> intent = colHeads().clone();
    return new Concept<G, M>(colAnd(intent), intent);
  }

  public final Concept<G, M> objectConcept(final G g)
  {
    final Set<M> intent = row(g);
    return new Concept<G, M>(colAnd(intent), intent);
  }

  public final Concept<G, M> attributeConcept(final M m)
  {
    final Set<G> extent = col(m);
    return new Concept<G, M>(extent, rowAnd(extent));
  }

  public final SetList<G> objectLabels(final Set<G> extent, final Set<M> intent)
  {
    return SetLists.filter(SetLists.intersection(rowHeads(), extent), new Predicate<G>()
      {
        public final boolean apply(final G object)
        {
          return row(object).equals(intent);
        }
      });
  }

  public final SetList<M> attributeLabels(final Set<G> extent, final Set<M> intent)
  {
    return SetLists.filter(SetLists.intersection(colHeads(), intent), new Predicate<M>()
      {
        public final boolean apply(final M attribute)
        {
          return col(attribute).equals(extent);
        }
      });
  }

  public final void selectAttribute(final M attribute)
  {
    ignoredAttributes.remove(attribute);
    select();
    push(new RelationEvent<G, M>(RelationEvent.SELECTION_CHANGED));
  }

  public final void deselectAttribute(final M attribute)
  {
    ignoredAttributes.add(attribute);
    select();
    push(new RelationEvent<G, M>(RelationEvent.SELECTION_CHANGED));
  }

  public final void selectAllAttributes()
  {
    ignoredAttributes.clear();
    select();
    push(new RelationEvent<G, M>(RelationEvent.SELECTION_CHANGED));
  }

  public final void deselectAllAttributes()
  {
    ignoredAttributes.addAll(colHeads());
    select();
    push(new RelationEvent<G, M>(RelationEvent.SELECTION_CHANGED));
  }

  public final Set<M> selectedAttributes()
  {
    return Sets.difference(colHeads, ignoredAttributes);
  }

  public final Context<G, M> subRelation(final Collection<?> objects, final Collection<?> attributes)
  {
    return new AbstractContext<G, M>(SetLists.intersection(rowHeads, objects), SetLists.intersection(
        colHeads,
        attributes), false)
      {
        public final boolean contains(final Object object, final Object attribute)
        {
          return rowHeads().contains(object) && colHeads().contains(attribute)
              && MatrixContext.this.contains(object, attribute);
        }

        public final MatrixContext<G, M> clone()
        {
          if (rowHeads().isEmpty() || colHeads().isEmpty())
            return new MatrixContext<G, M>(false);
          return new MatrixContext<G, M>(rowHeads(), colHeads(), (BooleanMatrix) MatrixContext.this.matrix.selectRows(
              Ret.NEW,
              MatrixContext.this.rowHeads().indicesOf(rowHeads(), false)).selectColumns(
              Ret.NEW,
              MatrixContext.this.colHeads().indicesOf(colHeads(), false)), false);
        }
      };
  }

  public final Context<M, G> dual()
  {
    return new AbstractContext<M, G>(colHeads(), rowHeads(), false)
      {
        public final boolean contains(final Object attribute, final Object object)
        {
          return !MatrixContext.this.contains(object, attribute);
        }

        public final MatrixContext<M, G> clone()
        {
          return new MatrixContext<M, G>(
              MatrixContext.this.colHeads().clone(),
              MatrixContext.this.rowHeads().clone(),
              BooleanMatrices.dual(matrix),
              false);
        }
      };
  }

  public final Context<G, M> complement()
  {
    return new AbstractContext<G, M>(rowHeads(), colHeads(), false)
      {
        public final boolean contains(final Object object, final Object attribute)
        {
          return !MatrixContext.this.contains(object, attribute);
        }

        public final MatrixContext<G, M> clone()
        {
          return new MatrixContext<G, M>(
              MatrixContext.this.rowHeads().clone(),
              MatrixContext.this.colHeads().clone(),
              BooleanMatrices.complement(matrix),
              false);
        }
      };
  }

  public final Context<M, G> contrary()
  {
    return new AbstractContext<M, G>(colHeads(), rowHeads(), false)
      {
        public final boolean contains(final Object attribute, final Object object)
        {
          return !MatrixContext.this.contains(object, attribute);
        }

        public final MatrixContext<M, G> clone()
        {
          return new MatrixContext<M, G>(
              MatrixContext.this.colHeads().clone(),
              MatrixContext.this.rowHeads().clone(),
              BooleanMatrices.complement(BooleanMatrices.dual(matrix)),
              false);
        }
      };
  }

  public final Relation<Pair<G, M>, Pair<G, M>> ferrersGraph()
  {
    final SetList<Pair<G, M>> vertices =
        SetLists.cartesianProduct(MatrixContext.this.rowHeads(), MatrixContext.this.colHeads()).filter(
            new Predicate<Pair<G, M>>()
              {
                public final boolean apply(final Pair<G, M> p)
                {
                  return !MatrixContext.this.contains(p.x(), p.y());
                }
              });
    return new AbstractRelation<Pair<G, M>, Pair<G, M>>(vertices, vertices, false)
      {
        public final boolean contains(final Object o1, final Object o2)
        {
          return MatrixContext.this.contains(((Pair<?, ?>) o1).x(), ((Pair<?, ?>) o2).y())
              && MatrixContext.this.contains(((Pair<?, ?>) o2).x(), ((Pair<?, ?>) o1).y());
        }
      };
  }

  public MatrixContext<G, M> clone()
  {
    return new MatrixContext<G, M>(rowHeads(), colHeads(), BooleanMatrices.clone(matrix), homogen);
  }

  public String toString()
  {
    if (id.get() == null)
      return super.toString();
    return id.get();
  }
}