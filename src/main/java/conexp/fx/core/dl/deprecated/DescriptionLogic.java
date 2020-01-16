package conexp.fx.core.dl.deprecated;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
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

@Deprecated
public enum DescriptionLogic {
  L0(Constructor.CONJUNCTION),
  EL(Constructor.CONJUNCTION, Constructor.EXISTENTIAL_RESTRICTION),
  FL0(Constructor.CONJUNCTION, Constructor.VALUE_RESTRICTION),
  FLE(Constructor.CONJUNCTION, Constructor.EXISTENTIAL_RESTRICTION, Constructor.VALUE_RESTRICTION),
  FLG(
      Constructor.CONJUNCTION,
      Constructor.EXISTENTIAL_RESTRICTION,
      Constructor.VALUE_RESTRICTION,
      Constructor.QUALIFIED_AT_LEAST_RESTRICTION),
  FLQ(
      Constructor.CONJUNCTION,
      Constructor.EXISTENTIAL_RESTRICTION,
      Constructor.VALUE_RESTRICTION,
      Constructor.QUALIFIED_AT_LEAST_RESTRICTION,
      Constructor.UNQUALIFIED_AT_MOST_RESTRICTION);

  public final Constructor[] constructors;

  DescriptionLogic(final Constructor... constructors) {
    this.constructors = constructors;
  }
}
