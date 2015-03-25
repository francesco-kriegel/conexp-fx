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

public enum Constructor {
  CONJUNCTION,
  EXISTENTIAL_RESTRICTION,
  VALUE_RESTRICTION,
  QUALIFIED_AT_LEAST_RESTRICTION,
  QUALIFIED_AT_MOST_RESTRICTION,
  PRIMITIVE_NEGATION,
  EXISTENTIAL_SELF_RESTRICTION,
  SIMPLE_ROLE_INCLUSION,
  COMPLEX_ROLE_INCLUSION;
}