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


public class StringData extends Data<String> {

  public StringData(final String key, final String value) throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.STRING, key, value);
  }

}
