package conexp.fx.experiment;

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

import java.io.File;

import conexp.fx.core.algorithm.nextclosures.NextClosures6;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CXTImporter2;

public class Foo {

  public static void main(String[] args) {
    MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("Contexts/scales/contranominal_5.cxt"));
    System.out.println(NextClosures6.compute(cxt, true).implications.size());;
  }

}