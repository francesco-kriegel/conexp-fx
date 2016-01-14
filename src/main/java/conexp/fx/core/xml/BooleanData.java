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


public class BooleanData extends Data<Boolean> {

  public BooleanData(final String key, final Boolean value) throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.BOOLEAN, key, value);
  }

  public BooleanData(final String key, final String value) throws NullPointerException, IndexOutOfBoundsException {
    this(key, Boolean.valueOf(value));
  }

}
