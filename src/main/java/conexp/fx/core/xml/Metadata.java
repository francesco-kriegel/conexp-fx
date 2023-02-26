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


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;

import conexp.fx.core.collections.Pair;

public final class Metadata extends Data<Element> {

  public Metadata(final Element element) throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.METADATA, "metadata", JsoupUtil.firstChildByTag(element, "metadata"));
  }

  public synchronized final String getSubkey() throws NullPointerException {
    return JsoupUtil.firstChildByTag(value, "subkey").text();
  }

  public synchronized final Map<String, Pair<Datatype, Metadata>> getKeyDatatypeMap() throws NullPointerException {
    final Map<String, Pair<Datatype, Metadata>> map = new HashMap<String, Pair<Datatype, Metadata>>();
    for (Element data : JsoupUtil.childrenByTag(value, "data")) {
      final String key = JsoupUtil.firstChildByTag(data, "key").text();
      final Datatype type = Datatype.valueOf(JsoupUtil.firstChildByTag(data, "type").text());
      switch (type) {
      case COMPOUND:
      case COMPOUND_LIST:
      case BOOLEAN_LIST:
      case INTEGER_LIST:
      case STRING_LIST:
        map.put(key, new Pair<Datatype, Metadata>(type, new Metadata(data)));
        break;
      default:
        map.put(key, new Pair<Datatype, Metadata>(type, null));
      }
    }
    return Collections.unmodifiableMap(map);
  }

}
