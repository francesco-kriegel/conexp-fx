package conexp.fx.core.dl;

import conexp.fx.core.util.UnicodeSymbols;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

public enum Constructor {
  CONJUNCTION("Conjunction", "C" + UnicodeSymbols.SQCAP + "D"),
  EXISTENTIAL_RESTRICTION("Existential Restriction", UnicodeSymbols.EXISTS + "r.C"),
  VALUE_RESTRICTION("Value Restriction", UnicodeSymbols.FORALL + "r.C"),
  QUALIFIED_AT_LEAST_RESTRICTION("Qualified At-Least Restriction", UnicodeSymbols.LEQ + "n.r.C"),
  QUALIFIED_AT_MOST_RESTRICTION("Qualified At-Most Restriction", UnicodeSymbols.GEQ + "n.r.C"),
  PRIMITIVE_NEGATION("Primitive Negation", UnicodeSymbols.NEG + "A"),
  EXISTENTIAL_SELF_RESTRICTION("Existential Self Restriction", UnicodeSymbols.EXISTS + "r.Self"),
  SIMPLE_ROLE_INCLUSION("Simple Role Inclusion", "r" + UnicodeSymbols.SQSUBSETEQ + "s"),
  COMPLEX_ROLE_INCLUSION("Complex Role Inclusion", "r1" + UnicodeSymbols.CIRC + "..." + UnicodeSymbols.CIRC + "rn"
      + UnicodeSymbols.SQSUBSETEQ + "s");

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
