package conexp.fx.gui.implication;

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

import java.util.Iterator;

import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.gui.FCAInstance;
import de.tudresden.inf.tcs.fcalib.Implication;

public class AttributeExploration {

  private final FCAInstance<String, String> tab;
  private Iterator<Implication<String>>     impls;

  public AttributeExploration(final FCAInstance<String, String> fcaInstance) {
    super();
    this.tab = fcaInstance;
    initialize();
    showNextImplication();
  }

  private void initialize() {
    NextImplication<String, String> nextImpl = new NextImplication<String, String>(tab.context);
    impls = nextImpl.iterator();
  }

  private void showNextImplication() {
    if (!impls.hasNext())
      return;
    final ExplorationDialog<String, String> explorationDialog =
        new ExplorationDialog<String, String>(tab, impls.next());
    if (explorationDialog.showAndReturn() == 0)
      showNextImplication();
  }
}
