package conexp.fx.core.builder;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.ConExpFX;

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

  public MatrixContext<G, M> createContext() {
    context = new MatrixContext<G, M>(type.homogen);
    return context;
  }

  public final MatrixContext<G, M> createContext(final MatrixContext.AutomaticMode automaticMode) {
    context = new MatrixContext<G, M>(type.homogen, automaticMode);
    return context;
  }

  public abstract void setContent() throws Exception;

  public String getId() {
    return type.title + " " + IdGenerator.getNextId(ConExpFX.instance);
  }
}
