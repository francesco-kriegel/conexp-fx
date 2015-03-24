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
