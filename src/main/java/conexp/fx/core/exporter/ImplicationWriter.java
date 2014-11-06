package conexp.fx.core.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.tudresden.inf.tcs.fcalib.Implication;

public class ImplicationWriter {

  public final static <M> void export(final List<Implication<M>> implications, final File file) throws IOException {
    System.out.println("writing " + implications.size() + " to " + file.getAbsolutePath());
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);
      bw.append("implications:\n");
      for (Implication<M> imp : implications)
        bw.append(imp.toString());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bw.close();
      fw.close();
    }
  }

}
