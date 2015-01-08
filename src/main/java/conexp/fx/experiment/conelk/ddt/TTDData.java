package conexp.fx.experiment.conelk.ddt;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;

import conexp.fx.core.util.FileUtil;
import conexp.fx.experiment.conelk.TupleFile;

public final class TTDData {

  private static final File getDataFile(final String name) {
    if (TTD_Example.base == null)
      return FileUtil.getFile(name, TTDData.class);
    else
      return new File(TTD_Example.base + "/" + name);
  }

  public static final File      READ_ME  = getDataFile("_TTD_README.txt");

  public static final TupleFile CLASS_A  = new TupleFile(getDataFile("A_TTD_DRUG.csv"), "\t") {

                                           @Override
                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugId = tuple[0];
                                             final String drugTypeNumber = tuple[1];
                                             final String drugName = tuple[2];
                                             statements.add(new StatementImpl(new TTD.Uri(drugId), RDF.TYPE, TTD.DRUG));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(drugId),
                                                 TTD.HAS_NAME,
                                                 new LiteralImpl(drugName)));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(drugId),
                                                 TTD.HAS_TYPE,
                                                 new TTD.Uri("DrugType" + drugTypeNumber)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile CLASS_B  = new TupleFile(getDataFile("B_TTD_DISEASE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String diseaseNumber = tuple[0];
                                             final String diseaseName = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("Disease" + diseaseNumber),
                                                 RDF.TYPE,
                                                 TTD.DISEASE));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("Disease" + diseaseNumber),
                                                 TTD.HAS_NAME,
                                                 new LiteralImpl(diseaseName)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile CLASS_B_ = new TupleFile(getDataFile("B_ACTUAL_TTD_DISEASE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String diseaseNumber = tuple[0];
                                             final String diseaseName = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("Disease" + diseaseNumber),
                                                 RDF.TYPE,
                                                 TTD.DISEASE));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("Disease" + diseaseNumber),
                                                 TTD.HAS_NAME,
                                                 new LiteralImpl(diseaseName)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile CLASS_C  = new TupleFile(getDataFile("C_TTD_TARGET.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String targetId = tuple[0];
                                             final String targetTypeNumber = tuple[1];
                                             final String targetName = tuple[2];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(targetId),
                                                 RDF.TYPE,
                                                 TTD.TARGET));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(targetId),
                                                 TTD.HAS_NAME,
                                                 new LiteralImpl(targetName)));
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(targetId),
                                                 TTD.HAS_TYPE,
                                                 new TTD.Uri("TargetType" + targetTypeNumber)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile LINK_1   = new TupleFile(getDataFile("1_TTD_DRUG_TTD_DISEASE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugId = tuple[0];
                                             final String diseaseNumber = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(drugId),
                                                 TTD.HEALS,
                                                 new TTD.Uri("Disease" + diseaseNumber)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile LINK_2   = new TupleFile(getDataFile("2_TTD_TARGET_TTD_DISEASE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String diseaseNumber = tuple[0];
                                             final String targetId = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("Disease" + diseaseNumber),
                                                 TTD.LOCATED_AT,
                                                 new TTD.Uri(targetId)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile LINK_3   = new TupleFile(getDataFile("3_BINDS_TTD_DRUG_TTD_TARGET.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugId = tuple[0];
                                             final String targetId = tuple[1];
                                             final String interactionNumber = tuple[2];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(drugId),
                                                 TTD.BINDS_TO,
                                                 new TTD.Uri(targetId)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile LINK_C   = new TupleFile(getDataFile("XC_MAP_TTD_TARGET_UNIPROT.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String targetId = tuple[0];
                                             final String uniprotId = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri(targetId),
                                                 TTD.MAPS_TO,
                                                 new TTD.Uri(uniprotId)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile TYPE_A   = new TupleFile(getDataFile("TA_MAP_TTD_DRUG_TYPE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugMetatypeNumber = tuple[0];
                                             final String drugTypeNumber = tuple[1];
                                             final String drugTypeName = tuple[2];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("DrugType" + drugTypeNumber),
                                                 TTD.HAS_TYPE,
                                                 new TTD.Uri("DrugMetatype" + drugMetatypeNumber)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile TYPE_A_  = new TupleFile(getDataFile("TA_RT_DRUG_TYPE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugMetatypeNumber = tuple[0];
                                             final String drugMetatypeName = tuple[1];
                                             statements
                                                 .add(new StatementImpl(
                                                     new TTD.Uri("DrugMetatype" + drugMetatypeNumber),
                                                     TTD.HAS_NAME,
                                                     new LiteralImpl(drugMetatypeName)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile TYPE_A__ = new TupleFile(getDataFile("TA_TTD_DRUG_TYPE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String drugTypeNumber = tuple[0];
                                             final String drugTypeName = tuple[1];
                                             statements.add(new StatementImpl(
                                                 new TTD.Uri("DrugType" + drugTypeNumber),
                                                 TTD.HAS_NAME,
                                                 new LiteralImpl(drugTypeName)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile TYPE_C   = new TupleFile(getDataFile("TC_RT_TARGET_TYPE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String targetTypeNumber = tuple[0];
                                             final String targetTypeName = tuple[1];
                                             statements.add(new StatementImpl(new TTD.Uri("TargetType"
                                                 + targetTypeNumber), TTD.HAS_NAME, new LiteralImpl(targetTypeName)));
                                             return statements;
                                           }
                                         };

  public static final TupleFile TYPE_3   = new TupleFile(getDataFile("T3_RT_TTD_INTERACTION_TYPE.csv"), "\t") {

                                           public Iterable<Statement> convert(String[] tuple) {
                                             final List<Statement> statements = new LinkedList<Statement>();
                                             final String interactionTypeNumber = tuple[0];
                                             final String interactionTypeName = tuple[1];
                                             statements.add(new StatementImpl(new TTD.Uri("InteractionType"
                                                 + interactionTypeNumber), TTD.HAS_NAME, new LiteralImpl(
                                                 interactionTypeName)));
                                             return statements;
                                           }
                                         };

}
