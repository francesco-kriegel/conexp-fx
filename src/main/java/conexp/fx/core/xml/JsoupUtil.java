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


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class JsoupUtil {

  private static final String USER_AGENT         =
                                                     "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17";
  private static final int    CONNECTION_TIMEOUT = 60000;

  public static final Document getDocument(final String url) throws MalformedURLException, IOException {
    return getDocument(new URL(url));
  }

  public static final Document getDocument(final URL url) throws IOException {
    return Jsoup
        .connect(url.toString())
        .ignoreContentType(true)
        .timeout(CONNECTION_TIMEOUT)
        .userAgent(USER_AGENT)
        .get();
  }

  public static final Element firstElement(final Element element, final String... tags) {
    Element _element = element;
    for (String tag : tags)
      _element = firstChildByTag(_element, tag);
    return _element;
  }

  public static final Element firstOrAppendElement(final Element element, final String... tags) {
    Element _element = element;
    for (String tag : tags)
      if (!childrenByTag(_element, tag).iterator().hasNext())
        _element = _element.appendElement(tag);
      else
        _element = firstChildByTag(_element, tag);
    return _element;
  }

  public static final Iterable<Element> childrenByTag(final Element element, final String tag) {
    return Iterables.filter(element.children(), new Predicate<Element>() {

      public final boolean apply(final Element _element) {
        return _element.tagName().equals(tag);
      }
    });
  }

  public static final Element firstChildByTag(final Element element, final String tag) {
    return Iterables.getFirst(childrenByTag(element, tag), null);
  }

}
