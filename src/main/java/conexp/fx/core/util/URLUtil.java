package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class URLUtil {

  public static final String readLines(final String url) throws IOException {
    return readLines(new URL(url));
  }

  public static final String readLines(final URL url) throws IOException {
    final InputStream stream = url.openStream();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String lines = "";
    String line;
    while ((line = reader.readLine()) != null)
      lines += line;
    reader.close();
    stream.close();
    return lines;
  }

}
