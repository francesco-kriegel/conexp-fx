package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

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
