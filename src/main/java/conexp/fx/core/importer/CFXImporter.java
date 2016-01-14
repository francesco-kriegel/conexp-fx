package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2016 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.io.File;
import java.io.IOException;
import java.util.Map;

import javafx.geometry.Point3D;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import conexp.fx.core.context.MatrixContext;

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
