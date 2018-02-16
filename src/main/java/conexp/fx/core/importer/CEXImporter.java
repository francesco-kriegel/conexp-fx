package conexp.fx.core.importer;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2018 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Point3D;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import conexp.fx.core.context.MatrixContext;

public class CEXImporter {

  public static void importt(MatrixContext<String, String> _context, Map<String, Point3D> seedMap, File file) {
    try {
      final Element xml = Jsoup.parse(file, null).body();
      final Element context =
          xml
              .getElementsByTag("ConceptualSystem")
              .first()
              .getElementsByTag("Contexts")
              .first()
              .getElementsByTag("Context")
              .first();
      final Element attributes = context.getElementsByTag("Attributes").first();
      final Element objects = context.getElementsByTag("Objects").first();
      final Map<Integer, String> attIdMap = new HashMap<Integer, String>();
      for (Element attributeEl : attributes.getElementsByTag("Attribute")) {
        final Integer id = Integer.valueOf(attributeEl.attr("Identifier"));
        final String attribute = attributeEl.getElementsByTag("Name").first().text();
        attIdMap.put(id, attribute);
        _context.colHeads().add(attribute);
      }
      int i = 0;
      for (Element objectEl : objects.getElementsByTag("Object")) {
        final String object = objectEl.getElementsByTag("Name").first().text();
        _context.rowHeads().add(object);
        System.out.println(i++ + "reading object " + object);
        for (Element hasAttributeEl : objectEl.getElementsByTag("Intent").first().getElementsByTag("HasAttribute")) {
          _context.addFastSilent(object, attIdMap.get(Integer.valueOf(hasAttributeEl.attr("AttributeIdentifier"))));
        }
      }
//          if (seedMap != null) {
//            // TODO extract seed vectors
//              for () {
//                  final String attribute = ;
//                  final Double x = ;
//                  final Double y = ;
//                  final Double z = ;
//                  final Point3D point3d = new Point3D(x, y, z);
//                  seedMap.put(attribute, point3d);
//              }
//          }
      _context.pushAllChangedEvent();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Unable to parse Concept Explorer file from " + file.toString());
      e.printStackTrace();
    }
  }
}
