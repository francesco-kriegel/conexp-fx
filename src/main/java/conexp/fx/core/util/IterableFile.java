package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.google.common.collect.Iterators;

public class IterableFile implements Iterable<String> {

  public static final Iterator<String> iterator(final File file) {
    return new IterableFile(file).iterator();
  }

  private final File file;

  public IterableFile(File file) {
    super();
    this.file = file;
  }

  /*
   * @see java.lang.Iterable#iterator() {@inheritDoc}
   */
  /**
   * The iterator uses a BufferedReader to read the file's contents, that is opened upon construction of the iterator
   * and closed just after the last line has been read. WARNING: This implies, that the BufferedReader is left unclosed,
   * if the file is not read completely, i.e. if next() is not called repeatedly until hasNext() returns false.
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
