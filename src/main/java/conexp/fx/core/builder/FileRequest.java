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
