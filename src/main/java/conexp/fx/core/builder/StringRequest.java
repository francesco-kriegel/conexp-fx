package conexp.fx.core.builder;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;

public abstract class StringRequest extends Request<String, String>
{
  public StringRequest(final Type type, final Source src)
  {
    super(type, src);
  }
}
