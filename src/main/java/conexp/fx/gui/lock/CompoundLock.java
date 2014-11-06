package conexp.fx.gui.lock;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
