package conexp.fx.core.xml;

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


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Element;

import conexp.fx.core.collections.pair.Pair;

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
