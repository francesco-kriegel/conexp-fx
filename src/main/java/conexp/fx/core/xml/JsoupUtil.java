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
