package conexp.fx.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

public final class IdGenerator {

  private static final IdGenerator             defaultKey = new IdGenerator();
  private static final Map<Object, AtomicLong> nextIds    = new ConcurrentHashMap<Object, AtomicLong>();

  private IdGenerator() {}

  public static final long getNextId() {
    return getNextId(defaultKey);
  }

  public static final long getNextId(final Object key) {
    synchronized (key) {
      return nextIds.computeIfAbsent(key, __ -> new AtomicLong(0l)).getAndIncrement();
    }
  }
}
