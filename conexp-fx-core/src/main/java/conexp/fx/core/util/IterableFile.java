package conexp.fx.core.util;

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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.google.common.collect.Iterators;

public class IterableFile implements Iterable<String> {

  private final File file;

  public IterableFile(File file) {
    super();
    this.file = file;
  }

  /*
   * @see java.lang.Iterable#iterator() {@inheritDoc}
   */
  public Iterator<String> iterator() {
    try {
      return new Iterator<String>() {

        private final FileReader     fileReader     = new FileReader(file);
        private final BufferedReader bufferedReader = new BufferedReader(fileReader);
        private String               nextLine       = bufferedReader.readLine();

        public boolean hasNext() {
          return nextLine != null;
        }

        public String next() {
          String next = this.nextLine;
          if (hasNext()) {
            try {
              this.nextLine = bufferedReader.readLine();
            } catch (IOException e) {
              e.printStackTrace();
              this.nextLine = null;
            }
            if (this.nextLine == null)
              try {
                bufferedReader.close();
                fileReader.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
          }
          return next;
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    } catch (IOException e) {
      e.printStackTrace();
      return Iterators.emptyIterator();
    }
  }
}
