package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Element;

public class CompoundData extends AbstractCompoundData {

  public CompoundData(final String key) {
    super(Datatype.COMPOUND, key, new ConcurrentHashMap<String, Data<?>>());
  }

  public CompoundData(final String key, final Map<String, Data<?>> value) {
    super(Datatype.COMPOUND, key, new ConcurrentHashMap<String, Data<?>>(value));
  }

  public CompoundData(final String key, final Element element, final Metadata metadata) throws NullPointerException,
      IndexOutOfBoundsException {
    super(Datatype.COMPOUND, key, element, metadata);
  }

}
