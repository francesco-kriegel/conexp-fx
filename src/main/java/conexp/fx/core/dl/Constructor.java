package conexp.fx.core.dl;

import conexp.fx.core.util.UnicodeSymbols;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

public enum Constructor {
  CONJUNCTION("Conjunction", "C" + UnicodeSymbols.SQCAP + "D"),
  EXISTENTIAL_RESTRICTION("Existential Restriction", UnicodeSymbols.EXISTS + "r.C"),
  VALUE_RESTRICTION("Value Restriction", UnicodeSymbols.FORALL + "r.C"),
  QUALIFIED_AT_LEAST_RESTRICTION("Qualified At-Least Restriction", UnicodeSymbols.GEQ + "n.r.C"),
  UNQUALIFIED_AT_MOST_RESTRICTION("Unqualified At-Most Restriction", UnicodeSymbols.LEQ + "n.r"),
  PRIMITIVE_NEGATION("Primitive Negation", UnicodeSymbols.NEG + "A"),
  EXISTENTIAL_SELF_RESTRICTION("Existential Self Restriction", UnicodeSymbols.EXISTS + "r.Self"),
  SIMPLE_ROLE_INCLUSION("Simple Role Inclusion", "r" + UnicodeSymbols.SQSUBSETEQ + "s"),
  COMPLEX_ROLE_INCLUSION(
      "Complex Role Inclusion",
      "r" + UnicodeSymbols.SUBSCRIPT_ONE + UnicodeSymbols.CIRC + "..." + UnicodeSymbols.CIRC + "r"
          + UnicodeSymbols.SUBSCRIPT_N + UnicodeSymbols.SQSUBSETEQ + "s");

  private String name;
  private String symbol;

  private Constructor(String name, String symbol) {
    this.name = name;
    this.symbol = symbol;
  }

  public String toString() {
    return name + " (" + symbol + ")";
  }

  public String getName() {
    return name;
  }

  public String getSymbol() {
    return symbol;
  }
}
