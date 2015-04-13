package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
