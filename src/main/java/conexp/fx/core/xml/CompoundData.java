package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
