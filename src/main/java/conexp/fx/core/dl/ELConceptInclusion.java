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

import conexp.fx.core.util.UnicodeSymbols;

public class ELConceptInclusion {

  private final ELConceptDescription subsumee;
  private final ELConceptDescription subsumer;

  public ELConceptInclusion(final ELConceptDescription subsumee, final ELConceptDescription subsumer) {
    super();
    this.subsumee = subsumee;
    this.subsumer = subsumer;
  }

  public final ELConceptDescription getSubsumee() {
    return subsumee;
  }

  public final ELConceptDescription getSubsumer() {
    return subsumer;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof ELConceptInclusion))
      return false;
    final ELConceptInclusion other = (ELConceptInclusion) obj;
    return this.subsumee.equals(other.subsumee) && this.subsumer.equals(other.subsumee);
  }

  @Override
  public int hashCode() {
    return 5 * subsumee.hashCode() + 7 * subsumer.hashCode();
  }

  @Override
  public String toString() {
    return subsumee.toString() + " " + UnicodeSymbols.SQSUBSETEQ + " " + subsumer.toString();
  }

}
