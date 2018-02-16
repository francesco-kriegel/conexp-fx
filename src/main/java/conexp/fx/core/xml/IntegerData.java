package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


public class IntegerData extends Data<Integer> {

  public IntegerData(final String key, final Integer value) throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.INTEGER, key, value);
  }

  public IntegerData(final String key, final String value) throws NullPointerException, IndexOutOfBoundsException {
    this(key, Integer.valueOf(value));
  }

}
