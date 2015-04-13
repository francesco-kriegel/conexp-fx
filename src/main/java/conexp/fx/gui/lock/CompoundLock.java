package conexp.fx.gui.lock;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CompoundLock extends ALock {

  public final Set<ALock> locks = new HashSet<ALock>();

  public CompoundLock(ALock... locks) {
    super("compound");
    this.locks.addAll(Arrays.asList(locks));
  }

  @Override
  public void lock() {
    for (ALock lock : locks)
      lock.lock();
  }

  @Override
  public void unlock() {
    for (ALock lock : locks)
      lock.unlock();
  }

  @Override
  public boolean isLocked() {
    for (ALock lock : locks)
      if (lock.isLocked())
        return true;
    return false;
  }
}
