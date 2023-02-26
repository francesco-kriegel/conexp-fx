package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2023 Francesco Kriegel
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
import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import conexp.fx.core.context.MatrixContext;
import javafx.geometry.Point3D;

public class CFXImporter {

  public static void importt(MatrixContext<String, String> _context, Map<String, Point3D> seedMap, File file) {
    try {
      final Element xml = Jsoup.parse(file, null).body();
      final Element domain = xml.getElementsByTag("domain").first();
      for (Element objectEl : domain.getElementsByTag("object")) {
        final String object = objectEl.attr("name");
        _context.rowHeads().add(object);
//        if (!Boolean.valueOf(objectEl.attr("selected")))
//          formalContext.getSelectedObjects().remove(object);
      }
      final Element codomain = xml.getElementsByTag("codomain").first();
      for (Element attributeEl : codomain.getElementsByTag("attribute")) {
        final String attribute = attributeEl.attr("name");
        _context.colHeads().add(attribute);
        if (!Boolean.valueOf(attributeEl.attr("selected")))
          _context.selectedAttributes().remove(attribute);
      }
      final Element context = xml.getElementsByTag("context").first();
      for (Element incidenceEl : context.getElementsByTag("incidence")) {
        final String object = incidenceEl.attr("object");
        final String attribute = incidenceEl.attr("attribute");
        _context.addFast(object, attribute);
      }
      if (seedMap != null) {
        final Element lattice = xml.getElementsByTag("lattice").first();
        for (Element seedEl : lattice.getElementsByTag("attribute-seed")) {
          final String attribute = seedEl.attr("attribute");
          final Double x = Double.valueOf(seedEl.attr("x"));
          final Double y = Double.valueOf(seedEl.attr("y"));
          final Double z = Double.valueOf(seedEl.attr("z"));
          final Point3D point3d = new Point3D(x, y, z);
          seedMap.put(attribute, point3d);
        }
      }
      _context.pushAllChangedEvent();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Unable to parse ConExpFX file from " + file.toString());
      e.printStackTrace();
    }
  }
}
