package conexp.fx.core.builder;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
