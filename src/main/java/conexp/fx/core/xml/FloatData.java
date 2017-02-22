package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


public class FloatData extends Data<Float> {

  public FloatData(final String key, final Float value) throws NullPointerException, IndexOutOfBoundsException {
    super(Datatype.FLOAT, key, value);
  }

  public FloatData(final String key, final String value) throws NullPointerException, IndexOutOfBoundsException {
    this(key, Float.valueOf(value));
  }

}
