package conexp.fx.core.xml;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import conexp.fx.core.collections.Collections3;

/**
 * Use an XMLFile for persistent configuration values. Each value is adressed by a structured key. Keys must be
 * lower-case strings (!!!) and are simply structured by dots, like URLs or names within ontologies.
 */
public final class XMLFile extends AbstractCompoundData {

  private final File file;

  public XMLFile(final File file) throws IOException {
    super(Datatype.DOCUMENT, "", readElementFromFile(file), new Metadata(readElementFromFile(file)));
    this.file = file;
  }

  public final File getFile() {
    return file;
  }

  private synchronized static final Element readElementFromFile(final File file) throws IOException {
    return Jsoup.parse(file, null).body();
  }

  public synchronized final void store() throws IOException {
    writeDocumentToFile(writeDataToDocument());
  }

  private synchronized final Document writeDataToDocument() {
    final Document document = new Document("");
    writeDataToElement(value, document, document.appendElement("metadata"));
    return document;
  }

  private synchronized final void
      writeDataToElement(final Map<String, Data<?>> map, final Element element, final Element metadata) {
    for (final String key : Collections3.sort(map.keySet())) {
      final Data<?> value = map.get(key);
      Element metadata_data = null;
      if (metadata != null) {
        metadata_data = metadata.appendElement("data");
        metadata_data.appendElement("key").text(key);
        metadata_data.appendElement("type").text(value.getType().toString());
        switch (value.getType()) {
        case BOOLEAN_LIST:
        case INTEGER_LIST:
        case FLOAT_LIST:
        case STRING_LIST:
        case COMPOUND_LIST:
          JsoupUtil.firstOrAppendElement(metadata_data, "metadata", "subkey").text(value.toListData().getSubkey());
        default:
        }
      }
      final Element data = JsoupUtil.firstOrAppendElement(element, Key.toArray(key));
      switch (value.getType()) {
      case BOOLEAN:
        data.text(value.toBooleanData().getValue().toString());
        break;
      case INTEGER:
        data.text(value.toIntegerData().getValue().toString());
        break;
      case FLOAT:
        data.text(value.toFloatData().getValue().toString());
        break;
      case STRING:
        data.text(value.toStringData().getValue());
        break;
      case COMPOUND:
        writeDataToElement(
            value.toCompoundData(),
            data,
            metadata != null ? JsoupUtil.firstOrAppendElement(metadata_data, "metadata") : null);
        break;
      case BOOLEAN_LIST:
        for (Boolean boolean_ : value.toBooleanListData())
          data.appendElement(value.toListData().getSubkey()).text(boolean_.toString());
        break;
      case INTEGER_LIST:
        for (Integer integer : value.toIntegerListData())
          data.appendElement(value.toListData().getSubkey()).text(integer.toString());
        break;
      case FLOAT_LIST:
        for (Float floating : value.toFloatListData())
          data.appendElement(value.toListData().getSubkey()).text(floating.toString());
        break;
      case STRING_LIST:
        for (String string : value.toStringListData())
          data.appendElement(value.toListData().getSubkey()).text(string);
        break;
      case COMPOUND_LIST:
        int i = 0;
        for (Map<String, Data<?>> map_ : value.toCompoundListData())
          writeDataToElement(
              map_,
              data.appendElement(value.toListData().getSubkey()),
              i++ == 0 && metadata != null ? JsoupUtil.firstOrAppendElement(metadata_data, "metadata") : null);
      case METADATA:
      default:
      }
    }
  }

  private synchronized final void writeDocumentToFile(final Document document) throws IOException {
    final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
    bufferedWriter.append(document.toString());
    bufferedWriter.close();
  }

  public static final void createEmptyConfiguration(File file) throws IOException {
//		file.getParentFile().mkdirs();
    file.createNewFile();
    final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
    bufferedWriter.append("<metadata>\r\n</metadata>");
    bufferedWriter.close();
  }
}
