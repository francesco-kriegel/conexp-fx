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
