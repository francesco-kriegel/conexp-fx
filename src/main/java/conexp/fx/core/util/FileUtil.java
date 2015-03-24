package conexp.fx.core.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class FileUtil {

  public static final File getFile(final String path, final Class<?> clazz) {
    return new File(clazz.getResource(path).getFile());
  }

  public static final String readLines(final String file) throws IOException {
    return readLines(new File(file));
  }

  public static final String readLines(final File file) throws IOException {
    final InputStream stream = new FileInputStream(file);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String lines = "";
    String line;
    while ((line = reader.readLine()) != null)
      lines += line;
    reader.close();
    stream.close();
    return lines;
  }

  public static final void moveFile(final File sourceFile, final File targetDirectory, final String targetName)
      throws IOException {
    if (!sourceFile.exists() || !sourceFile.isFile())
      throw new FileNotFoundException("No file found at " + sourceFile);
    if (targetDirectory.exists() && targetDirectory.isFile())
      throw new FileNotFoundException("Directory expected, but file found at " + targetDirectory);
    targetDirectory.mkdirs();
    com.google.common.io.Files.move(sourceFile, new File(targetDirectory, targetName));
//    java.nio.file.Files.move(
//        sourceFile.toPath(),
//        new File(targetDirectory, targetName).toPath(),
//        StandardCopyOption.ATOMIC_MOVE);
  }

  public static final void moveDirectory(final File sourceDirectory, final File targetDirectory) throws IOException {
    if (!sourceDirectory.exists() || !sourceDirectory.isDirectory())
      throw new FileNotFoundException("No directory found at " + sourceDirectory);
    if (targetDirectory.exists() && targetDirectory.isFile())
      throw new FileNotFoundException("Directory expected, but file found at " + targetDirectory);
    targetDirectory.mkdirs();
    Files.move(
        sourceDirectory.toPath(),
        new File(targetDirectory, sourceDirectory.getName()).toPath(),
        StandardCopyOption.ATOMIC_MOVE);
  }

  public static final File longestFile(final File directory) {
    File longestFile = null;
    long maxLength = 0;
    for (File file : directory.listFiles()) {
      final long length = file.length();
      if (length > maxLength) {
        longestFile = file;
        maxLength = length;
      }
    }
    return longestFile;
  }

  public static void delete(final File directory, final FileFilter fileFilter) throws IOException {
    if (!directory.exists() || !directory.isDirectory())
      throw new FileNotFoundException("No directory found at " + directory);
    for (File file : directory.listFiles(fileFilter))
      Files.delete(file.toPath());
  }

  public static final boolean deleteIfEmpty(final File directory) throws IOException {
    if (directory.list().length > 0)
      return false;
    Files.delete(directory.toPath());
    return true;
  }
}
