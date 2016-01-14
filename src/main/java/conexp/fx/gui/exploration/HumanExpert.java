package conexp.fx.gui.exploration;

import java.util.Collections;
import java.util.Set;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import conexp.fx.core.algorithm.nextclosure.exploration.CounterExample;
import conexp.fx.core.algorithm.nextclosure.exploration.Expert;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.implication.Implication;
import conexp.fx.gui.dialog.FXDialog.Return;

public final class HumanExpert implements Expert<String, String> {

  private final MatrixContext<String, String> context;

  public HumanExpert(final MatrixContext<String, String> context) {
    super();
    this.context = context;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public final Set<CounterExample<String, String>> askForCounterExample(final Implication<String, String> implication)
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
