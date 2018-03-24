package conexp.fx.gui.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
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

import java.util.Collections;
import java.util.Set;

import conexp.fx.core.algorithm.exploration.CounterExample;
import conexp.fx.core.algorithm.exploration.Expert;
import conexp.fx.core.context.Implication;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.gui.dialog.FXDialog.Return;

public final class HumanExpert implements Expert<String, String> {

  private final MatrixContext<String, String> context;

  public HumanExpert(final MatrixContext<String, String> context) {
    super();
    this.context = context;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public final Set<CounterExample<String, String>> getCounterExamples(final Implication<String, String> implication)
      throws InterruptedException {
    final Return<CounterExample<String, String>> result = new CounterExampleDialog(context, implication).showAndWait();
    switch (result.result()) {
    case YES:
      return Collections.emptySet();
    case NO:
      return Collections.singleton(result.value());
    case CANCEL:
      throw new InterruptedException("Attribute Exploration has been interrupted by user.");
    }
    return null;
  }

}
