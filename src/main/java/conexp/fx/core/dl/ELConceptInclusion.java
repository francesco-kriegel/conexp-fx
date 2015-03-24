package conexp.fx.core.dl;

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
