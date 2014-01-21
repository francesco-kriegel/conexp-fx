package conexp.fx.gui.context;

/*
 * #%L
 * Concept Explorer FX - Graphical User Interface
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


import conexp.fx.core.util.IdGenerator;
import conexp.fx.gui.tab.CFXTab;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;

public class StringMatrixContextWidget extends MatrixContextWidget<String, String>
{
  public StringMatrixContextWidget(final CFXTab<String, String> conExpTab)
  {
    super(conExpTab);
    final Button domainButton =
        ButtonBuilder
            .create()
            .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Object")
            .onAction(new EventHandler<ActionEvent>()
            {
              public void handle(ActionEvent event)
              {
                conExpTab.fca.addObject((conExpTab.fca.context.isHomogen() ? "Element " : "Object ")
                    + IdGenerator.getNextId());
              }
            })
            .build();
    domainButton.minWidthProperty().bind(rowHeaderPane.columnWidth);
    domainButton.maxWidthProperty().bind(rowHeaderPane.columnWidth);
    domainButton.minHeightProperty().bind(cellSize);
    domainButton.maxHeightProperty().bind(cellSize);
    domainButton.styleProperty().bind(new StringBinding()
    {
      {
        super.bind(textSize);
      }

      @Override
      protected String computeValue()
      {
        return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
      }
    });
    centerPane.add(domainButton, 0, 2);
    final Button codomainButton =
        ButtonBuilder
            .create()
            .text(conExpTab.fca.context.isHomogen() ? "New Element" : "New Attribute")
            .onAction(new EventHandler<ActionEvent>()
            {
              public void handle(ActionEvent event)
              {
                conExpTab.fca.addAttribute((conExpTab.fca.context.isHomogen() ? "Element " : "Attribute ")
                    + IdGenerator.getNextId());
              }
            })
            .build();
    codomainButton.rotateProperty().set(-90);
    codomainButton.minWidthProperty().bind(colHeaderPane.rowHeight);
    codomainButton.maxWidthProperty().bind(colHeaderPane.rowHeight);
    codomainButton.minHeightProperty().bind(cellSize);
    codomainButton.maxHeightProperty().bind(cellSize);
    codomainButton.translateXProperty().bind(new DoubleBinding()
    {
      {
        super.bind(colHeaderPane.rowHeight, cellSize);
      }

      @Override
      protected double computeValue()
      {
        return -(colHeaderPane.rowHeight.get() - cellSize.get()) / 2d;
      }
    });
    codomainButton.styleProperty().bind(new StringBinding()
    {
      {
        super.bind(textSize);
      }

      @Override
      protected String computeValue()
      {
        return "-fx-padding: 0; -fx-font-size: " + textSize.get() + ";";
      }
    });
    centerPane.add(codomainButton, 2, 0);
  }
}