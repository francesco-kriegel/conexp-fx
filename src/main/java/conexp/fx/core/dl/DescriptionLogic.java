package conexp.fx.core.dl;

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
      Constructor.QUALIFIED_AT_MOST_RESTRICTION);

  public final Constructor[] constructors;

  DescriptionLogic(final Constructor... constructors) {
    this.constructors = constructors;
  }
}