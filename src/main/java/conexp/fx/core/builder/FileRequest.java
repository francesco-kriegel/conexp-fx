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


import java.io.File;

import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;

public abstract class FileRequest extends StringRequest
{
  public final File file;

  protected FileRequest(final Type type, final File file)
  {
    super(type, Source.FILE);
    this.file = file;
  }

  public String getId()
  {
    return file.getName();
  }
}
