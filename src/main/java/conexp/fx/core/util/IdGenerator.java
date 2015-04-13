package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


public final class IdGenerator {

  private static long nextId = 0;

  public static final synchronized long getNextId() {
    return nextId++;
  }
}
