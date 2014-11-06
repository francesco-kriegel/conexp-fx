/*
 * @author Francesco.Kriegel@gmx.de
 */
package conexp.fx.core.importer;

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


import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ujmp.core.booleanmatrix.BooleanMatrix;

import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.IterableFile;

public class CXTImporter {

  public static void importt(MatrixContext<String, String> context, File file) {
    importt(context, new IterableFile(file));
  }

  public static void importt(MatrixContext<String, String> context, String content) {
    importt(context, Arrays.asList(content.split("\\r?\\n")));
  }

  public static void importt(MatrixContext<String, String> context, Iterable<String> lines) {
    int linenumber = 0;
    int objectCount = 0;
    int attributeCount = 0;
    List<String> objects = new LinkedList<String>();
    List<String> attributes = new LinkedList<String>();
    BooleanMatrix matrix = null;
    for (String line : lines) {
      if (line != null && !line.startsWith("EOF")) {
        if (linenumber == 2) {
          objectCount = Integer.parseInt(line);
          System.out.println(objectCount + " objects");
        }
        if (linenumber == 3) {
          attributeCount = Integer.parseInt(line);
          System.out.println(attributeCount + " attributes");
        }
        if (linenumber >= 5 && linenumber < 5 + objectCount)
          objects.add(line);
        if (linenumber == 5 + objectCount)
          context.rowHeads().addAll(objects);
        if (linenumber >= 5 + objectCount && linenumber < 5 + objectCount + attributeCount)
          attributes.add(line);
        if (linenumber == 5 + objectCount + attributeCount) {
          context.colHeads().addAll(attributes);
          matrix = context.matrix();
        }
        if (linenumber >= 5 + objectCount + attributeCount
            && linenumber < 5 + objectCount + attributeCount + objectCount) {
          int row = linenumber - (5 + objectCount + attributeCount);
          int column = 0;
          for (char c : line.toCharArray()) {
            if (c == 'X')
              matrix.setBoolean(true, row, column);
            column++;
          }
        }
        linenumber++;
      }
    }
    context.pushAllChangedEvent();
  }
}
