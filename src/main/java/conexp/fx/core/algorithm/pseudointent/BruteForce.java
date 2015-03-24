package conexp.fx.core.algorithm.pseudointent;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;


public class BruteForce {

  /**
   * 
   * computes all pseudo-intents of a {@link MatrixContext}. This method may be slow, it is only intended for testing
   * purposes.
   * 
   * Definition: For a given context {@code (G,M,I)} a {@literal pseudo-intent} is a subset {@code B} of the codomain
   * {@code M}, such that it is no intent (i.e. {@code B≠B''}) and furthermore each strictly smaller pseudo-intent
   * {@code D⊊B} has its closure in {@code B} (i.e. {@code D''⊆B}).
   * 
   * @param a {@link MatrixContext} cxt
   * @return a {@link Map} whose keys are all pseudo-intents and their intents as values
   */
  public final static <G, M> Map<Set<M>, Set<M>> pseudoIntents(final MatrixContext<G, M> cxt) {
    final Map<Set<M>, Set<M>> pseudoIntents = new HashMap<Set<M>, Set<M>>();
    // computes all pseudo-intents from smallest to biggest
    for (Set<M> attributes : SetLists.powerSet(cxt.colHeads())) {
      Set<M> intent = cxt.intent(attributes);
      // checks whether attributes is already an intent
      if (attributes.containsAll(intent))
        continue;
      // checks if all strictly smaller pseudo-intents have their closure in attributes
      boolean isPseudoIntent = true;
      for (Entry<Set<M>, Set<M>> pseudoIntent : pseudoIntents.entrySet())
        if (attributes.size() > pseudoIntent.getKey().size() && attributes.containsAll(pseudoIntent.getKey())
            && !attributes.containsAll(pseudoIntent.getValue())) {
          // no pseudo-intent
          isPseudoIntent = false;
          break;
        }
      if (isPseudoIntent) {
        intent = new HashSet<M>(intent);
        intent.removeAll(attributes);
        pseudoIntents.put(attributes, intent);
      }
    }
    return pseudoIntents;
  }

}
