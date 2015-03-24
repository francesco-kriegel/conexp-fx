package conexp.fx.core.builder;

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

import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IdGenerator;

public abstract class Request<G, M> {

  public final Type             type;
  public final Source           src;
  protected MatrixContext<G, M> context;

  public Request(final Type type, final Source src) {
    super();
    if (!type.sources.contains(src))
      throw new IllegalArgumentException();
    this.type = type;
    this.src = src;
  }

  public final MatrixContext<G, M> createContext() {
    context = new MatrixContext<G, M>(type.homogen);
    return context;
  }

  public final MatrixContext<G, M> createContext(final MatrixContext.AutomaticMode automaticMode) {
    context = new MatrixContext<G, M>(type.homogen, automaticMode);
    return context;
  }

  public abstract void setContent();

  public String getId() {
    return type.title + " " + IdGenerator.getNextId();
  }
}
