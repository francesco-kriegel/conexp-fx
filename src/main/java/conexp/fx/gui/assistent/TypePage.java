package conexp.fx.gui.assistent;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2022 Francesco Kriegel
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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.openrdf.repository.http.HTTPRepository;

import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Construct.Apposition;
import conexp.fx.core.builder.Requests.Construct.BiProduct;
import conexp.fx.core.builder.Requests.Construct.Complement;
import conexp.fx.core.builder.Requests.Construct.Contrary;
import conexp.fx.core.builder.Requests.Construct.DirectProduct;
import conexp.fx.core.builder.Requests.Construct.DirectSum;
import conexp.fx.core.builder.Requests.Construct.Dual;
import conexp.fx.core.builder.Requests.Construct.HorizontalSum;
import conexp.fx.core.builder.Requests.Construct.Quadposition;
import conexp.fx.core.builder.Requests.Construct.SemiProduct;
import conexp.fx.core.builder.Requests.Construct.Subposition;
import conexp.fx.core.builder.Requests.Construct.SubstitutionSum;
import conexp.fx.core.builder.Requests.Construct.VerticalSum;
import conexp.fx.core.builder.Requests.Import.ImportCEX;
import conexp.fx.core.builder.Requests.Import.ImportCFX;
import conexp.fx.core.builder.Requests.Import.ImportCSVB;
import conexp.fx.core.builder.Requests.Import.ImportCXT;
import conexp.fx.core.builder.Requests.Import.ImportSPARQLFromEndpoint;
import conexp.fx.core.builder.Requests.Import.ImportSPARQLFromFile;
import conexp.fx.core.builder.Requests.Import.ImportSPARQLFromRepository;
import conexp.fx.core.builder.Requests.Import.ImportSPARQLFromURL;
import conexp.fx.core.builder.Requests.New.NewContext;
import conexp.fx.core.builder.Requests.New.NewOrder;
import conexp.fx.core.builder.Requests.Scale.BiOrdinalScale;
import conexp.fx.core.builder.Requests.Scale.BooleanScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.BooleanScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.ContraNominalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.ContraNominalScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.ContraOrdinalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.ContraOrdinalScaleFromOrder;
import conexp.fx.core.builder.Requests.Scale.ContraOrdinalScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.ConvexOrdinalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.ConvexOrdinalScaleFromOrder;
import conexp.fx.core.builder.Requests.Scale.ConvexOrdinalScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.DichtomicScale;
import conexp.fx.core.builder.Requests.Scale.GridScale;
import conexp.fx.core.builder.Requests.Scale.InterOrdinalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.InterOrdinalScaleFromOrder;
import conexp.fx.core.builder.Requests.Scale.InterOrdinalScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.NominalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.NominalScaleFromSetList;
import conexp.fx.core.builder.Requests.Scale.OrdinalScaleFromInt;
import conexp.fx.core.builder.Requests.Scale.OrdinalScaleFromSetList;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.util.FileFormat;
import conexp.fx.gui.ConExpFX;
import conexp.fx.gui.dataset.FCADataset;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.InsetsBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.text.FontBuilder;
import jfxtras.scene.control.ListSpinner;

@SuppressWarnings("deprecation")
public class TypePage extends AssistentPage<Request<?, ?>> {

  public final class FileChooseBox extends HBox {

    public final ObjectProperty<Pair<File, FileFormat>> fileProperty =
        new SimpleObjectProperty<Pair<File, FileFormat>>(null);

    public FileChooseBox(final String title, final FileFormat... fileFormats) {
      super();
      this.setAlignment(Pos.BASELINE_LEFT);
      this.setPadding(new Insets(2));
      this.setSpacing(8);
      final Label chosen = LabelBuilder.create().build();
      chosen.textProperty().bind(new StringBinding() {

        {
          bind(fileProperty);
        }

        protected final String computeValue() {
          if (fileProperty.get() == null)
            return "unchosen";
          return fileProperty.get().first().getName();
        }
      });
      final Button choose = ButtonBuilder.create().text("Choose File...").onAction(new EventHandler<ActionEvent>() {

        public final void handle(final ActionEvent event) {
          fileProperty.set(ConExpFX.instance.showOpenFileDialog(title, fileFormats));
        }
      }).build();
      this.getChildren().addAll(chosen, choose);
    }
  }

  private final class SourcePane extends TitledPane {

    protected final ObservableValue<Request<?, ?>> result;

    private SourcePane(final Source src, final Node content, final ObservableValue<Request<?, ?>> result) {
      super(src.title, content);
      this.result = result;
      this.setFont(FontBuilder.create().size(16).build());
    }
  }

  private final Type      type;
  private final Accordion accordion = new Accordion();

  public TypePage(final Type type) {
    super(type.title, type.description, null, null);
    this.contentProperty.set(accordion);
    this.type = type;
    if (type.sources.contains(Source.NULL))
      this.resultProperty.set(null);
    for (Source source : type.sources)
      this.accordion.getPanes().add(createSourcePane(source));
    this.accordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {

      public void changed(ObservableValue<? extends TitledPane> observable, TitledPane oldValue, TitledPane newValue) {
        if (newValue == null) {
          TypePage.this.resultProperty.unbind();
          TypePage.this.resultProperty.set(null);
        } else
          TypePage.this.resultProperty.bind(((SourcePane) newValue).result);
      }
    });
    this.accordion.setExpandedPane(accordion.getPanes().get(0));
  }

  private final SourcePane createSourcePane(final Source src) {
    switch (src) {
    case NULL:
      return nullPane();
    case INT_INT:
      return intIntPane();
    case INT:
      return intPane();
    case INT_LIST:
      return intListPane();
    case STRINGS:
      return stringsPane();
    case ORDER:
      return orderPane();
    case ORDER_ORDER:
      return orderOrderPane();
    case CONTEXT:
      return contextPane();
    case CONTEXT_DOUBLE:
      return contextDoublePane();
    case CONTEXT_CONTEXT:
      return contextContextPane();
    case CONTEXT_CONTEXT_CONTEXT_CONTEXT:
      return contextContextContextContextPane();
    case CONTEXT_CONTEXT_OBJECT_OBJECT:
      return contextContextObjectObjectPane();
    case CONTEXT_SET_SET:
      return contextSetSetPane();
    case FILE:
      return filePane();
    case SPARQL_AND_XMLURL:
      return queryURLPane();
    case SPARQL_AND_ONTOLOGYFILE:
      return queryFilePane();
    case SPARQL_AND_ONTOLOGYURL:
      return queryURLPane2();
    case SPARQL_AND_ONTOLOGYREPOSITORY:
      return queryRepositoryPane();
    }
    return null;
  }

  private GridPane newGridPane() {
    return GridPaneBuilder
        .create()
        .hgap(10)
        .vgap(10)
        .maxWidth(480)
        .padding(InsetsBuilder.create().left(10).right(10).top(10).bottom(10).build())
        .build();
  }

  private final SourcePane nullPane() {
    return new SourcePane(Source.NULL, newGridPane(), new SimpleObjectProperty<Request<?, ?>>(new DichtomicScale()));
  }

  private final SourcePane intIntPane() {
    final GridPane pane = newGridPane();
    final ListSpinner<Integer> objectSpinner = new ListSpinner<Integer>(0, 1024);
    final ListSpinner<Integer> attributeSpinner = new ListSpinner<Integer>(0, 1024);
    pane.add(LabelBuilder.create().text("Number of Objects: ").build(), 0, 0);
    pane.add(objectSpinner, 1, 0);
    pane.add(LabelBuilder.create().text("Number of Attribute): ").build(), 0, 1);
    pane.add(attributeSpinner, 1, 1);
    return new SourcePane(Source.INT_INT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(objectSpinner.valueProperty(), attributeSpinner.valueProperty());
      }

      protected final Request<?, ?> computeValue() {
        return new NewContext(objectSpinner.getValue(), attributeSpinner.getValue());
      }
    });
  }

  private final SourcePane intPane() {
    final GridPane pane = newGridPane();
    final ListSpinner<Integer> elementSpinner = new ListSpinner<Integer>(0, 1024);
    pane.add(LabelBuilder.create().text("Number of Elements: ").build(), 0, 0);
    pane.add(elementSpinner, 1, 0);
    return new SourcePane(Source.INT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(elementSpinner.valueProperty());
      }

      protected final Request<?, ?> computeValue() {
        return new NewOrder(elementSpinner.getValue());
      }
    });
  }

  private final SourcePane intListPane() {
    final GridPane pane = newGridPane();
    final ListSpinner<Integer> nSpinner = new ListSpinner<Integer>(0, 1024);
    nSpinner.setMinWidth(100);
    pane.add(LabelBuilder.create().text("n = ").build(), 0, 0);
    pane.add(nSpinner, 1, 0);
    return new SourcePane(Source.INT_LIST, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(nSpinner.valueProperty());
      }

      protected final Request<?, ?> computeValue() {
        switch (type) {
        case BOOLEAN:
          return new BooleanScaleFromInt(nSpinner.getValue());
        case NOMINAL:
          return new NominalScaleFromInt(nSpinner.getValue());
        case CONTRA_NOMINAL:
          return new ContraNominalScaleFromInt(nSpinner.getValue());
        case ORDINAL:
          return new OrdinalScaleFromInt(nSpinner.getValue());
        case CONTRA_ORDINAL:
          return new ContraOrdinalScaleFromInt(nSpinner.getValue());
        case INTER_ORDINAL:
          return new InterOrdinalScaleFromInt(nSpinner.getValue());
        case CONVEX_ORDINAL:
          return new ConvexOrdinalScaleFromInt(nSpinner.getValue());
//            case BINARY_RELATIONS_CONTEXT:
//              return new BinaryRelationsFromInt(nSpinner.getValue());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane stringsPane() {
    final GridPane pane = newGridPane();
    final TextField textField = TextFieldBuilder.create().minWidth(260).maxWidth(260).build();
    pane.add(LabelBuilder.create().text("Semicolon separated Elements:").build(), 0, 0);
    pane.add(textField, 1, 0);
    return new SourcePane(Source.STRINGS, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(textField.textProperty());
      }

      protected final Request<?, ?> computeValue() {
        final HashSetArrayList<String> s =
            new HashSetArrayList<String>(Arrays.asList(textField.textProperty().get().split(";")));
        switch (type) {
        case BOOLEAN:
          return new BooleanScaleFromSetList<String>(s);
        case NOMINAL:
          return new NominalScaleFromSetList<String>(s);
        case CONTRA_NOMINAL:
          return new ContraNominalScaleFromSetList<String>(s);
        case ORDINAL:
          return new OrdinalScaleFromSetList<String>(s);
        case CONTRA_ORDINAL:
          return new ContraOrdinalScaleFromSetList<String>(s);
        case INTER_ORDINAL:
          return new InterOrdinalScaleFromSetList<String>(s);
        case CONVEX_ORDINAL:
          return new ConvexOrdinalScaleFromSetList<String>(s);
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane orderPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> orderChoice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.orders)
        .build();
    pane.add(LabelBuilder.create().text("Order Context:").build(), 0, 0);
    pane.add(orderChoice, 1, 0);
    return new SourcePane(Source.ORDER, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(orderChoice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
        case CONTRA_ORDINAL:
          return new ContraOrdinalScaleFromOrder(orderChoice.valueProperty().get());
        case INTER_ORDINAL:
          return new InterOrdinalScaleFromOrder(orderChoice.valueProperty().get());
        case CONVEX_ORDINAL:
          return new ConvexOrdinalScaleFromOrder(orderChoice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane orderOrderPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> order1Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.orders)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> order2Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.orders)
        .build();
    pane.add(LabelBuilder.create().text("First Order Context:").build(), 0, 0);
    pane.add(order1Choice, 1, 0);
    pane.add(LabelBuilder.create().text("Second Order Context:").build(), 0, 1);
    pane.add(order2Choice, 1, 1);
    return new SourcePane(Source.ORDER_ORDER, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(order1Choice.valueProperty(), order2Choice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
        case BI_ORDINAL:
          return new BiOrdinalScale(order1Choice.valueProperty().get(), order2Choice.valueProperty().get());
        case GRID:
          return new GridScale(order1Choice.valueProperty().get(), order2Choice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane contextPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> contextChoice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    pane.add(LabelBuilder.create().text("Formal Context:").build(), 0, 0);
    pane.add(contextChoice, 1, 0);
    return new SourcePane(Source.CONTEXT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(contextChoice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
        case COMPLEMENT:
          return new Complement(contextChoice.valueProperty().get());
        case DUAL:
          return new Dual(contextChoice.valueProperty().get());
        case CONTRARY:
          return new Contrary(contextChoice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane contextDoublePane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> contextChoice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    pane.add(LabelBuilder.create().text("Formal Context:").build(), 0, 0);
    pane.add(contextChoice, 1, 0);
    final ListSpinner<Integer> toleranceSpinner = new ListSpinner<Integer>(0, 100);
    pane.add(LabelBuilder.create().text("Tolerance (%): ").build(), 0, 1);
    pane.add(toleranceSpinner, 1, 1);
    return new SourcePane(Source.CONTEXT_DOUBLE, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(contextChoice.valueProperty(), toleranceSpinner.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
//            case APPROXIMATION_CONTEXT_BY_ATTRIBUTES:
//              return new AttributeApproximation(contextChoice.valueProperty().get(), toleranceSpinner
//                  .valueProperty()
//                  .get() / 100d);
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane contextContextPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> context1Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> context2Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    pane.add(LabelBuilder.create().text("First Formal Context:").build(), 0, 0);
    pane.add(context1Choice, 1, 0);
    pane.add(LabelBuilder.create().text("Second Formal Context:").build(), 0, 1);
    pane.add(context2Choice, 1, 1);
    return new SourcePane(Source.CONTEXT_CONTEXT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(context1Choice.valueProperty(), context2Choice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
        case APPOSITION:
          return new Apposition(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case SUBPOSITION:
          return new Subposition(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case HORIZONTAL_SUM:
          return new HorizontalSum(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case VERTICAL_SUM:
          return new VerticalSum(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case DIRECT_SUM:
          return new DirectSum(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case DIRECT_PRODUCT:
          return new DirectProduct(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case BI_PRODUCT:
          return new BiProduct(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        case SEMI_PRODUCT:
          return new SemiProduct(context1Choice.valueProperty().get(), context2Choice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane contextContextContextContextPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> context1Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> context2Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> context3Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> context4Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    pane.add(LabelBuilder.create().text("First Formal Context:").build(), 0, 0);
    pane.add(context1Choice, 1, 0);
    pane.add(LabelBuilder.create().text("Second Formal Context:").build(), 0, 1);
    pane.add(context2Choice, 1, 1);
    pane.add(LabelBuilder.create().text("Third Formal Context:").build(), 0, 2);
    pane.add(context3Choice, 1, 2);
    pane.add(LabelBuilder.create().text("Fourth Formal Context:").build(), 0, 3);
    pane.add(context4Choice, 1, 3);
    return new SourcePane(Source.CONTEXT_CONTEXT_CONTEXT_CONTEXT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(
            context1Choice.valueProperty(),
            context2Choice.valueProperty(),
            context3Choice.valueProperty(),
            context4Choice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
        case QUADPOSITION:
          return new Quadposition(
              context1Choice.valueProperty().get(),
              context2Choice.valueProperty().get(),
              context3Choice.valueProperty().get(),
              context4Choice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private final SourcePane contextContextObjectObjectPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> context1Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox<MatrixContext<?, ?>> context2Choice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    final ChoiceBox objectChoice = ChoiceBoxBuilder.create().minWidth(320).maxWidth(320).build();
    final ChoiceBox attributeChoice = ChoiceBoxBuilder.create().minWidth(320).maxWidth(320).build();
    objectChoice.itemsProperty().bind(new ObjectBinding<ObservableList<?>>() {

      {
        bind(context1Choice.valueProperty());
      }

      protected ObservableList<?> computeValue() {
        if (context1Choice.valueProperty().get() == null)
          return FXCollections.observableList(Collections.EMPTY_LIST);
        return FXCollections.observableList(context1Choice.valueProperty().get().rowHeads());
      }
    });
    attributeChoice.itemsProperty().bind(new ObjectBinding<ObservableList<?>>() {

      {
        bind(context1Choice.valueProperty());
      }

      protected ObservableList<?> computeValue() {
        if (context1Choice.valueProperty().get() == null)
          return FXCollections.observableList(Collections.EMPTY_LIST);
        return FXCollections.observableList(context1Choice.valueProperty().get().colHeads());
      }
    });
    pane.add(LabelBuilder.create().text("First Formal Context:").build(), 0, 0);
    pane.add(context1Choice, 1, 0);
    pane.add(LabelBuilder.create().text("Second Formal Context:").build(), 0, 1);
    pane.add(context2Choice, 1, 1);
    pane.add(LabelBuilder.create().text("First Object:").build(), 0, 2);
    pane.add(objectChoice, 1, 2);
    pane.add(LabelBuilder.create().text("Second Attribute:").build(), 0, 3);
    pane.add(attributeChoice, 1, 3);
    return new SourcePane(Source.CONTEXT_CONTEXT_OBJECT_OBJECT, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(
            context1Choice.valueProperty(),
            context2Choice.valueProperty(),
            objectChoice.valueProperty(),
            attributeChoice.valueProperty());
      }

      protected final Request<?, ?> computeValue() {
        switch (type) {
        case SUBSTITUTION_SUM:
          return new SubstitutionSum(
              context1Choice.valueProperty().get(),
              context2Choice.valueProperty().get(),
              objectChoice.valueProperty().get(),
              attributeChoice.valueProperty().get());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane contextSetSetPane() {
    final GridPane pane = newGridPane();
    final ChoiceBox<MatrixContext<?, ?>> contextChoice = ChoiceBoxBuilder
        .<MatrixContext<?, ?>> create()
        .minWidth(320)
        .maxWidth(320)
        .items(ConExpFX.instance.contexts)
        .build();
    pane.add(LabelBuilder.create().text("Formal Context:").build(), 0, 0);
    pane.add(contextChoice, 1, 0);
    // TODO
    return new SourcePane(Source.CONTEXT_SET_SET, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(contextChoice.valueProperty());
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected final Request<?, ?> computeValue() {
        switch (type) {
//            case APPROXIMATION_CONTEXT:
//              return new Approximation(contextChoice.valueProperty().get(), null, null);
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane filePane() {
    final GridPane pane = newGridPane();
    final FileChooseBox fileChooseBox;
    switch (type) {
    case IMPORT_CFX_CONTEXT:
      fileChooseBox = new FileChooseBox("Import Concept Explorer FX File", FileFormat.CFX);
      break;
    case IMPORT_CXT_CONTEXT:
      fileChooseBox = new FileChooseBox("Import Burmeister File", FileFormat.CXT);
      break;
    case IMPORT_CEX_CONTEXT:
      fileChooseBox = new FileChooseBox("Import Concept Explorer File", FileFormat.CEX);
      break;
    case IMPORT_CSV_CONTEXT:
      fileChooseBox = new FileChooseBox("Import Binary CSV File", FileFormat.CSVB);
      break;
    default:
      fileChooseBox = null;
      break;
    }
    pane.add(LabelBuilder.create().text("Concept Explorer File: ").build(), 0, 0);
    pane.add(fileChooseBox, 1, 0);
    return new SourcePane(Source.FILE, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(fileChooseBox.fileProperty);
      }

      protected final Request<?, ?> computeValue() {
        if (fileChooseBox.fileProperty.get() == null)
          return null;
        switch (type) {
        case IMPORT_CFX_CONTEXT:
          return new ImportCFX(fileChooseBox.fileProperty.get().first());
        case IMPORT_CXT_CONTEXT:
          return new ImportCXT(fileChooseBox.fileProperty.get().first());
        case IMPORT_CEX_CONTEXT:
          return new ImportCEX(fileChooseBox.fileProperty.get().first());
        case IMPORT_CSV_CONTEXT:
          return new ImportCSVB(fileChooseBox.fileProperty.get().first());
        default:
          return null;
        }
      }
    });
  }

  private final SourcePane queryURLPane() {
    final GridPane pane = newGridPane();
    pane.getColumnConstraints().add(0, new ColumnConstraints(80));
    final ChoiceBox<String> sampleURLChoice = ChoiceBoxBuilder
        .<String> create()
        .items(FXCollections.observableArrayList("FactForge.net", "LinkedLifeData.com"))
        .build();
    final TextField urlField = TextFieldBuilder
        .create()
        .text(
            "http://factforge.net/sparql.xml?query=<QUERY>&_implicit=false&implicit=true&_equivalent=false&_form=%2Fsparql")
        .build();
    final TextArea queryArea = TextAreaBuilder.create().minHeight(230).build();
    sampleURLChoice.valueProperty().addListener(new ChangeListener<String>() {

      public final void
          changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
        switch (newValue) {
        case "FactForge.net":
          urlField.setText(
              "http://factforge.net/sparql.xml?query=<QUERY>&_implicit=false&implicit=true&_equivalent=false&_form=%2Fsparql");
          break;
        case "LinkedLifeData.com":
          urlField.setText(
              "http://linkedlifedata.com/sparql.xml?query=<QUERY>&_implicit=false&implicit=true&_form=%2Fsparql");
          break;
        }
      }
    });
    pane.add(
        LabelBuilder
            .create()
            .wrapText(true)
            .text(
                "Enter an URL that computes a SPARQL XML Result. Specify the position of the SPARQL Query with the variable <QUERY>. Within the Query, use the variables ?object and ?attribute to indicate the elements of the constructed formal context. Furthermore use numbered variables, e.g. ?object1, ?object2, ..., if tuples should be created.")
            .build(),
        1,
        0);
    pane.add(sampleURLChoice, 1, 1);
    pane.add(LabelBuilder.create().text("URL: ").build(), 0, 2);
    pane.add(urlField, 1, 2);
    pane.add(LabelBuilder.create().text("SPARQL Query: ").build(), 0, 3);
    pane.add(queryArea, 1, 3);
    return new SourcePane(Source.SPARQL_AND_XMLURL, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(urlField.textProperty(), queryArea.textProperty());
      }

      protected final Request<?, ?> computeValue() {
        return new ImportSPARQLFromEndpoint(urlField.textProperty().get(), queryArea.textProperty().get());
      }
    });
  }

  private final SourcePane queryURLPane2() {
    final GridPane pane = newGridPane();
    pane.getColumnConstraints().add(0, new ColumnConstraints(80));
    final TextField urlField = TextFieldBuilder.create().build();
    final TextArea queryArea = TextAreaBuilder.create().minHeight(230).build();
    pane.add(
        LabelBuilder
            .create()
            .wrapText(true)
            .text(
                "Enter an URL of an ontology on the web. Within the Query, use the variables ?object and ?attribute to indicate the elements of the constructed formal context. Furthermore use numbered variables, e.g. ?object1, ?object2, ..., if tuples should be created.")
            .build(),
        1,
        0);
    pane.add(LabelBuilder.create().text("URL: ").build(), 0, 1);
    pane.add(urlField, 1, 1);
    pane.add(LabelBuilder.create().text("SPARQL Query: ").build(), 0, 2);
    pane.add(queryArea, 1, 2);
    return new SourcePane(Source.SPARQL_AND_ONTOLOGYURL, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(urlField.textProperty(), queryArea.textProperty());
      }

      protected final Request<?, ?> computeValue() {
        return new ImportSPARQLFromURL(urlField.textProperty().get(), queryArea.textProperty().get());
      }
    });
  }

  private final SourcePane queryFilePane() {
    final GridPane pane = newGridPane();
    pane.getColumnConstraints().add(0, new ColumnConstraints(80));
    final FileChooseBox fileChooseBox = new FileChooseBox("Select Ontology File", FileFormat.ANY);
    final TextArea queryArea = TextAreaBuilder.create().minHeight(230).build();
    pane.add(
        LabelBuilder
            .create()
            .wrapText(true)
            .text(
                "Choose a local ontology file. Within the Query, use the variables ?object and ?attribute to indicate the elements of the constructed formal context. Furthermore use numbered variables, e.g. ?object1, ?object2, ..., if tuples should be created.")
            .build(),
        1,
        0);
    pane.add(LabelBuilder.create().text("Ontology File: ").build(), 0, 1);
    pane.add(fileChooseBox, 1, 1);
    pane.add(LabelBuilder.create().text("SPARQL Query: ").build(), 0, 2);
    pane.add(queryArea, 1, 2);
    return new SourcePane(Source.SPARQL_AND_ONTOLOGYFILE, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(fileChooseBox.fileProperty, queryArea.textProperty());
      }

      protected final Request<?, ?> computeValue() {
        if (fileChooseBox.fileProperty.get() == null)
          return null;
        return new ImportSPARQLFromFile(fileChooseBox.fileProperty.get().first(), queryArea.textProperty().get());
      }
    });
  }

  private final SourcePane queryRepositoryPane() {
    final GridPane pane = newGridPane();
    pane.getColumnConstraints().add(0, new ColumnConstraints(105));
    final TextField urlField = TextFieldBuilder.create().build();
    final TextField repositoryField = TextFieldBuilder.create().build();
    final TextArea queryArea = TextAreaBuilder.create().minHeight(230).build();
    pane.add(
        LabelBuilder
            .create()
            .wrapText(true)
            .text(
                "Enter an URL of a Sesame Server and a name of a Repository. Within the Query, use the variables ?object and ?attribute to indicate the elements of the constructed formal context. Furthermore use numbered variables, e.g. ?object1, ?object2, ..., if tuples should be created.")
            .build(),
        1,
        0);
    pane.add(LabelBuilder.create().text("Sesame Server URL: ").build(), 0, 1);
    pane.add(urlField, 1, 1);
    pane.add(LabelBuilder.create().text("Repository Name: ").build(), 0, 2);
    pane.add(repositoryField, 1, 2);
    pane.add(LabelBuilder.create().text("SPARQL Query: ").build(), 0, 3);
    pane.add(queryArea, 1, 3);
    return new SourcePane(Source.SPARQL_AND_ONTOLOGYREPOSITORY, pane, new ObjectBinding<Request<?, ?>>() {

      {
        bind(urlField.textProperty(), repositoryField.textProperty(), queryArea.textProperty());
      }

      protected final Request<?, ?> computeValue() {
        return new ImportSPARQLFromRepository(
            new HTTPRepository(urlField.textProperty().get(), repositoryField.textProperty().get()),
            queryArea.textProperty().get());
      }
    });
  }

  protected final void onNext() {
    ConExpFX.instance.treeView.addDataset(new FCADataset(null, resultProperty.get()));
  }
}
