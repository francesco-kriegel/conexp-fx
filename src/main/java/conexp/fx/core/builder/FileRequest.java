package conexp.fx.core.builder;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
