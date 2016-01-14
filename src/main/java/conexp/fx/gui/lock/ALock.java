package conexp.fx.gui.lock;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

public abstract class ALock {

  private final String id;

  protected ALock(final String id) {
    super();
    this.id = id;
  }

  public final String getID() {
    return id;
  }

  public abstract void lock();

  public abstract void unlock();

  public abstract boolean isLocked();

}
