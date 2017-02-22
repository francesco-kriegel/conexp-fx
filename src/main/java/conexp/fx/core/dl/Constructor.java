package conexp.fx.core.dl;

import conexp.fx.core.util.UnicodeSymbols;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
