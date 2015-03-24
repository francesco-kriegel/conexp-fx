package conexp.fx.gui.assistent;

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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.InsetsBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.TitledPaneBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradientBuilder;
import javafx.scene.paint.StopBuilder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.FontBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import conexp.fx.core.builder.Requests.Metatype;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.util.ColorScheme;

public class ConstructAssistent
  extends AssistentPage<Type>
{
  private final ConExpFX                conExp;
  private final Map<String, AssistentPage<?>>    availablePages            =
                                                                               new ConcurrentHashMap<String, AssistentPage<?>>();
  private final ObjectProperty<AssistentPage<?>> currentPage               =
                                                                               new SimpleObjectProperty<AssistentPage<?>>(
                                                                                   this);
  private final ObjectProperty<Object>           currentResultProperty     = new SimpleObjectProperty<Object>();
  private final StringProperty                   currentNextPageIdProperty = new SimpleStringProperty();
  private final ListProperty<AssistentPage<?>>   previousPages             =
                                                                               new SimpleListProperty<AssistentPage<?>>(
                                                                                   FXCollections
                                                                                       .<AssistentPage<?>> observableArrayList());
  private final int                              width                     = 500;
  private final int                              height                    = 700;
  private final Stage                            stage                     = StageBuilder
                                                                               .create()
                                                                               .title("Construction Wizard")
                                                                               .build();
  private final BorderPane                       pane                      = BorderPaneBuilder.create().build();
  private final Text                             title                     = TextBuilder
                                                                               .create()
                                                                               .font(
                                                                                   FontBuilder
                                                                                       .create()
                                                                                       .size(20)
                                                                                       .build())
                                                                               .build();
  private final Text                             text                      = TextBuilder
                                                                               .create()
                                                                               .font(
                                                                                   FontBuilder
                                                                                       .create()
                                                                                       .size(16)
                                                                                       .build())
                                                                               .wrappingWidth(width - 50)
                                                                               .build();
  private Accordion                              accordion                 = new Accordion();                                      ;

  public ConstructAssistent(ConExpFX conExp)
  {
    super("Construct a new context", "Choose a construction type", null, new Function<Type, String>()
      {
        @Override
        public String apply(Type constructionType)
        {
          return constructionType.toString();
        }
      });
    this.conExp = conExp;
    contentProperty.set(createInitialNode());
    stage.initOwner(this.conExp.primaryStage);
    stage.initStyle(StageStyle.UTILITY);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setScene(SceneBuilder.create().width(width).height(height).root(pane).build());
    createPages();
    createTop();
    createBottom();
    title.textProperty().bind(new StringBinding()
      {
        {
          super.bind(currentPage);
        }

        @Override
        protected String computeValue()
        {
          return currentPage.get().titleProperty.get();
        }
      });
    text.textProperty().bind(new StringBinding()
      {
        {
          super.bind(currentPage);
        }

        @Override
        protected String computeValue()
        {
          return currentPage.get().textProperty.get();
        }
      });
    pane.centerProperty().bind(new ObjectBinding<Node>()
      {
        {
          super.bind(currentPage);
        }

        @Override
        protected Node computeValue()
        {
          return currentPage.get().contentProperty.get();
        }
      });
  }

  public void showAndWait()
  {
    stage.showAndWait();
  }

  private final void next()
  {
    currentPage.get().onNext();
    previousPages.add(currentPage.get());
    if (currentPage.get().nextPageIdBinding.get() != null) {
      currentPage.set(availablePages.get(currentPage.get().nextPageIdBinding.get()));
      currentResultProperty.bind(currentPage.get().resultProperty);
      currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
    } else
      stage.close();
  }

  private final void previous()
  {
    currentPage.set(previousPages.remove(previousPages.size() - 1));
    currentResultProperty.bind(currentPage.get().resultProperty);
    currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
  }

  private final void cancel()
  {
    stage.close();
  }

  private final Node createInitialNode()
  {
    currentResultProperty.bind(currentPage.get().resultProperty);
    currentNextPageIdProperty.bind(currentPage.get().nextPageIdBinding);
    final ToggleGroup radioGroup = new ToggleGroup();
    for (Type constructionType : Type.values()) {
      final RadioButton radioButton =
          RadioButtonBuilder
              .create()
              .userData(constructionType)
              .text(constructionType.title)
              .font(FontBuilder.create().size(16).build())
              .build();
      radioGroup.getToggles().add(radioButton);
    }
    resultProperty.bind(new ObjectBinding<Type>()
      {
        {
          super.bind(radioGroup.selectedToggleProperty());
        }

        @Override
        protected Type computeValue()
        {
          if (radioGroup.selectedToggleProperty().get() == null)
            return null;
          return (Type) radioGroup.selectedToggleProperty().get().getUserData();
        }
      });
    for (final Metatype constructionMetatype : Metatype.values())
//      if (constructionMetatype != Metatype.OTHER)
        accordion.getPanes().add(
            TitledPaneBuilder
                .create()
                .text(constructionMetatype.title)
                .font(FontBuilder.create().size(16).build())
                .content(
                    VBoxBuilder
                        .create()
                        .children(
                            Collections2.filter(
                                Collections2.transform(radioGroup.getToggles(), new Function<Toggle, RadioButton>()
                                  {
                                    @Override
                                    public RadioButton apply(Toggle toggle)
                                    {
                                      return (RadioButton) toggle;
                                    }
                                  }),
                                new Predicate<RadioButton>()
                                  {
                                    @Override
                                    public boolean apply(RadioButton radioButton)
                                    {
                                      return ((Type) radioButton.getUserData()).type.equals(constructionMetatype);
                                    }
                                  }))
                        .build())
                .build());
    accordion.setExpandedPane(accordion.getPanes().get(0));
    return accordion;
  }

  private final void createPages()
  {
    for (Type type : Type.values())
//      if (type.type != Metatype.OTHER)
        availablePages.put(type.toString(), new TypePage(this.conExp, type));
  }

  private final void createTop()
  {
    final Rectangle background =
        RectangleBuilder
            .create()
            .fill(
                LinearGradientBuilder
                    .create()
                    .startX(0)
                    .startY(0)
                    .endX(0)
                    .endY(1)
                    .cycleMethod(CycleMethod.NO_CYCLE)
                    .proportional(true)
                    .stops(
                        StopBuilder.create().color(ColorScheme.JAVA_FX.getColor(4)).offset(0).build(),
                        StopBuilder.create().color(Color.WHITE).offset(1).build())
                    .build())
            .height(80)
            .width(width)
            .build();
    pane.setTop(StackPaneBuilder
        .create()
        .children(
            background,
            VBoxBuilder
                .create()
                .alignment(Pos.TOP_LEFT)
                .padding(InsetsBuilder.create().top(10).left(10).build())
                .children(title, text)
                .build())
        .build());
  }

  private final void createBottom()
  {
    final Rectangle background =
        RectangleBuilder
            .create()
            .fill(
                LinearGradientBuilder
                    .create()
                    .startX(0)
                    .startY(0)
                    .endX(0)
                    .endY(1)
                    .cycleMethod(CycleMethod.NO_CYCLE)
                    .proportional(true)
                    .stops(
                        StopBuilder.create().color(Color.LIGHTGRAY).offset(1).build(),
                        StopBuilder.create().color(Color.WHITE).offset(0).build())
                    .build())
            .height(50)
            .width(width)
            .build();
    final Button cancel =
        ButtonBuilder.create().text("Cancel").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>()
          {
            @Override
            public void handle(ActionEvent event)
            {
              cancel();
            }
          }).build();
    final Button previous =
        ButtonBuilder.create().text("< Previous").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>()
          {
            @Override
            public void handle(ActionEvent event)
            {
              previous();
            }
          }).build();
    previous.disableProperty().bind(new BooleanBinding()
      {
        {
          super.bind(previousPages);
        }

        @Override
        protected boolean computeValue()
        {
          return previousPages.isEmpty();
        }
      });
    final Button next =
        ButtonBuilder.create().text("Next >").minHeight(20).minWidth(100).onAction(new EventHandler<ActionEvent>()
          {
            @Override
            public void handle(ActionEvent event)
            {
              next();
            }
          }).build();
    next.disableProperty().bind(new BooleanBinding()
      {
        {
          super.bind(currentResultProperty);
        }

        @Override
        protected boolean computeValue()
        {
          return currentResultProperty.isNull().get();
        }
      });
    next.textProperty().bind(new StringBinding()
      {
        {
          super.bind(currentResultProperty, currentNextPageIdProperty);
        }

        @Override
        protected String computeValue()
        {
          if (currentResultProperty.isNotNull().get() && currentNextPageIdProperty.isNull().get())
            return "Finish";
          else
            return "Next >";
        }
      });
    pane.setBottom(StackPaneBuilder
        .create()
        .children(
            background,
            HBoxBuilder
                .create()
                .alignment(Pos.CENTER_RIGHT)
                .spacing(10)
                .padding(InsetsBuilder.create().right(30).build())
                .children(cancel, previous, next)
                .build())
        .build());
  }

  @Override
  protected void onNext()
  {}
}
