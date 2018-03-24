package conexp.fx.gui.assistent;

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

import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.TitledPaneBuilder;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.FontBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import conexp.fx.core.builder.Requests.Metatype;
import conexp.fx.core.builder.Requests.Type;
import conexp.fx.gui.ConExpFX;

@SuppressWarnings("deprecation")
public class ConstructAssistent extends Assistent<Type> {

  private final Accordion accordion = new Accordion();

  public ConstructAssistent() {
    super(
        ConExpFX.instance.primaryStage,
        "Construction Wizard",
        "Construct a new context",
        "Choose a construction type",
        null,
        new Function<Type, String>() {

          @Override
          public String apply(Type constructionType) {
            return constructionType.toString();
          }
        });
    initialize();
  }

  @Override
  protected final Node createInitialNode() {
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
    resultProperty.bind(new ObjectBinding<Type>() {

      {
        super.bind(radioGroup.selectedToggleProperty());
      }

      @Override
      protected Type computeValue() {
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
                              Collections2.transform(radioGroup.getToggles(), new Function<Toggle, RadioButton>() {

                                @Override
                                public RadioButton apply(Toggle toggle) {
                                  return (RadioButton) toggle;
                                }
                              }),
                              new Predicate<RadioButton>() {

                                @Override
                                public boolean apply(RadioButton radioButton) {
                                  return ((Type) radioButton.getUserData()).type.equals(constructionMetatype);
                                }
                              }))
                      .build())
              .build());
    accordion.setExpandedPane(accordion.getPanes().get(0));
    return accordion;
  }

  @Override
  protected final void createPages() {
    for (Type type : Type.values())
//      if (type.type != Metatype.OTHER)
      availablePages.put(type.toString(), new TypePage(type));
  }

  @Override
  protected void onNext() {}
}
