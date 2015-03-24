package conexp.fx.core;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import conexp.fx.core.algorithm.nextclosures.NextClosures6C;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.core.importer.CXTImporter2;

public class ConstrainedImplicationsTest {

  public static final void main(String[] args) {
    final MatrixContext<String, String> cxt = new MatrixContext<String, String>(false);
    CXTImporter2.read(cxt, new File("/Users/francesco/workspace/Java/conexp-fx/relmmsc.cxt"));
    final Set<Implication<String, String>> backgroundImplications = new HashSet<Implication<String, String>>();

    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("1"), Sets.newHashSet("2"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("1"), Sets.newHashSet("3"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("1"), Sets.newHashSet("4"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("1"), Sets.newHashSet("5"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("1"), Sets.newHashSet("6"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("4"), Sets.newHashSet("6"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("5"), Sets.newHashSet("6"), Collections
        .emptySet()));
    backgroundImplications.add(new Implication<String, String>(Sets.newHashSet("2"), Sets.newHashSet("5"), Collections
        .emptySet()));

    Map<Set<String>, Set<String>> implicationalBase =
        NextClosures6C.computeWithBackgroundImplications(cxt, backgroundImplications, true).implications;
    for (Entry<Set<String>, Set<String>> entry : implicationalBase.entrySet())
      System.out.println(entry.getKey() + " ==> " + entry.getValue());
  }

}
