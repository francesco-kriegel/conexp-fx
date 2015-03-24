package conexp.fx.experiment.conelk.ddt;

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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import conexp.fx.core.util.FileUtil;
import conexp.fx.experiment.conelk.OWLTupleFile;

public final class OWLTTDData {

  private static final File getDataFile(final String name) {
    if (TTD_Example.base == null)
      return FileUtil.getFile(name, OWLTTDData.class);
    else
      return new File(TTD_Example.base + "/" + name);
  }

  public static final File         READ_ME  = getDataFile("_TTD_README.txt");

  public static final OWLTupleFile CLASS_A  = new OWLTupleFile(getDataFile("A_TTD_DRUG.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                                final String drugId = tuple[0];
                                                final String drugTypeNumber = tuple[1];
                                                final String drugName = tuple[2];
                                                OWLNamedIndividual individual =
                                                    factory.getOWLNamedIndividual(new OWLTTD.Iri(drugId));
                                                axioms.add(factory.getOWLClassAssertionAxiom(
                                                    factory.getOWLClass(OWLTTD.DRUG),
                                                    individual));
                                                axioms.add(factory.getOWLDataPropertyAssertionAxiom(
                                                    factory.getOWLDataProperty(OWLTTD.HAS_NAME),
                                                    individual,
                                                    drugName));
                                                axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
                                                    factory.getOWLObjectProperty(OWLTTD.HAS_TYPE),
                                                    individual,
                                                    factory.getOWLNamedIndividual(new OWLTTD.Iri("DrugType"
                                                        + drugTypeNumber))));
                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugId = tuple[0];
//                                             final String drugTypeNumber = tuple[1];
//                                             final String drugName = tuple[2];
//                                             statements.add(new StatementImpl(new TTD.Uri(drugId), RDF.TYPE, TTD.DRUG));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(drugId),
//                                                 TTD.HAS_NAME,
//                                                 new LiteralImpl(drugName)));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(drugId),
//                                                 TTD.HAS_TYPE,
//                                                 new TTD.Uri("DrugType" + drugTypeNumber)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile CLASS_B  = new OWLTupleFile(getDataFile("B_TTD_DISEASE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                                final String diseaseNumber = tuple[0];
                                                final String diseaseName = tuple[1];
                                                final OWLNamedIndividual individual =
                                                    factory.getOWLNamedIndividual(new OWLTTD.Iri("Disease"
                                                        + diseaseNumber));
                                                axioms.add(factory.getOWLClassAssertionAxiom(
                                                    factory.getOWLClass(OWLTTD.DISEASE),
                                                    individual));
                                                axioms.add(factory.getOWLDataPropertyAssertionAxiom(
                                                    factory.getOWLDataProperty(OWLTTD.HAS_NAME),
                                                    individual,
                                                    diseaseName));
                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String diseaseNumber = tuple[0];
//                                             final String diseaseName = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("Disease" + diseaseNumber),
//                                                 RDF.TYPE,
//                                                 TTD.DISEASE));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("Disease" + diseaseNumber),
//                                                 TTD.HAS_NAME,
//                                                 new LiteralImpl(diseaseName)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile CLASS_B_ = new OWLTupleFile(getDataFile("B_ACTUAL_TTD_DISEASE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                                final String diseaseNumber = tuple[0];
                                                final String diseaseName = tuple[1];
                                                
                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String diseaseNumber = tuple[0];
//                                             final String diseaseName = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("Disease" + diseaseNumber),
//                                                 RDF.TYPE,
//                                                 TTD.DISEASE));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("Disease" + diseaseNumber),
//                                                 TTD.HAS_NAME,
//                                                 new LiteralImpl(diseaseName)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile CLASS_C  = new OWLTupleFile(getDataFile("C_TTD_TARGET.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String targetId = tuple[0];
                                              final String targetTypeNumber = tuple[1];
                                              final String targetName = tuple[2];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String targetId = tuple[0];
//                                             final String targetTypeNumber = tuple[1];
//                                             final String targetName = tuple[2];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(targetId),
//                                                 RDF.TYPE,
//                                                 TTD.TARGET));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(targetId),
//                                                 TTD.HAS_NAME,
//                                                 new LiteralImpl(targetName)));
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(targetId),
//                                                 TTD.HAS_TYPE,
//                                                 new TTD.Uri("TargetType" + targetTypeNumber)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile LINK_1   = new OWLTupleFile(getDataFile("1_TTD_DRUG_TTD_DISEASE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String drugId = tuple[0];
                                              final String diseaseNumber = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugId = tuple[0];
//                                             final String diseaseNumber = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(drugId),
//                                                 TTD.HEALS,
//                                                 new TTD.Uri("Disease" + diseaseNumber)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile LINK_2   = new OWLTupleFile(getDataFile("2_TTD_TARGET_TTD_DISEASE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String diseaseNumber = tuple[0];
                                              final String targetId = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String diseaseNumber = tuple[0];
//                                             final String targetId = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("Disease" + diseaseNumber),
//                                                 TTD.LOCATED_AT,
//                                                 new TTD.Uri(targetId)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile LINK_3   = new OWLTupleFile(getDataFile("3_BINDS_TTD_DRUG_TTD_TARGET.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String drugId = tuple[0];
                                              final String targetId = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugId = tuple[0];
//                                             final String targetId = tuple[1];
//                                             final String interactionNumber = tuple[2];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(drugId),
//                                                 TTD.BINDS_TO,
//                                                 new TTD.Uri(targetId)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile LINK_C   = new OWLTupleFile(getDataFile("XC_MAP_TTD_TARGET_UNIPROT.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String targetId = tuple[0];
                                              final String uniprotId = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String targetId = tuple[0];
//                                             final String uniprotId = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri(targetId),
//                                                 TTD.MAPS_TO,
//                                                 new TTD.Uri(uniprotId)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile TYPE_A   = new OWLTupleFile(getDataFile("TA_MAP_TTD_DRUG_TYPE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String drugMetatypeNumber = tuple[0];
                                              final String drugTypeNumber = tuple[1];
                                              final String drugTypeName = tuple[2];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugMetatypeNumber = tuple[0];
//                                             final String drugTypeNumber = tuple[1];
//                                             final String drugTypeName = tuple[2];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("DrugType" + drugTypeNumber),
//                                                 TTD.HAS_TYPE,
//                                                 new TTD.Uri("DrugMetatype" + drugMetatypeNumber)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile TYPE_A_  = new OWLTupleFile(getDataFile("TA_RT_DRUG_TYPE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String drugMetatypeNumber = tuple[0];
                                              final String drugMetatypeName = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugMetatypeNumber = tuple[0];
//                                             final String drugMetatypeName = tuple[1];
//                                             statements
//                                                 .add(new StatementImpl(
//                                                     new TTD.Uri("DrugMetatype" + drugMetatypeNumber),
//                                                     TTD.HAS_NAME,
//                                                     new LiteralImpl(drugMetatypeName)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile TYPE_A__ = new OWLTupleFile(getDataFile("TA_TTD_DRUG_TYPE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String drugTypeNumber = tuple[0];
                                              final String drugTypeName = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String drugTypeNumber = tuple[0];
//                                             final String drugTypeName = tuple[1];
//                                             statements.add(new StatementImpl(
//                                                 new TTD.Uri("DrugType" + drugTypeNumber),
//                                                 TTD.HAS_NAME,
//                                                 new LiteralImpl(drugTypeName)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile TYPE_C   = new OWLTupleFile(getDataFile("TC_RT_TARGET_TYPE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                              final String targetTypeNumber = tuple[0];
                                              final String targetTypeName = tuple[1];

                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String targetTypeNumber = tuple[0];
//                                             final String targetTypeName = tuple[1];
//                                             statements.add(new StatementImpl(new TTD.Uri("TargetType"
//                                                 + targetTypeNumber), TTD.HAS_NAME, new LiteralImpl(targetTypeName)));
//                                             return statements;
//                                           }
                                            };

  public static final OWLTupleFile TYPE_3   = new OWLTupleFile(getDataFile("T3_RT_TTD_INTERACTION_TYPE.csv"), "\t") {

                                              @Override
                                              public Iterable<OWLAxiom> convert(String[] tuple, OWLDataFactory factory) {
                                                final List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
                                                final String interactionTypeNumber = tuple[0];
                                                final String interactionTypeName = tuple[1];
                                                axioms.add(factory.getOWLDataPropertyAssertionAxiom(
                                                    factory.getOWLDataProperty(OWLTTD.HAS_NAME),
                                                    factory.getOWLNamedIndividual(new OWLTTD.Iri("InteractionType"
                                                        + interactionTypeNumber)),
                                                    interactionTypeName));
                                                return axioms;
                                              }

//                                           public Iterable<Statement> convert(String[] tuple) {
//                                             final List<Statement> statements = new LinkedList<Statement>();
//                                             final String interactionTypeNumber = tuple[0];
//                                             final String interactionTypeName = tuple[1];
//                                             statements.add(new StatementImpl(new TTD.Uri("InteractionType"
//                                                 + interactionTypeNumber), TTD.HAS_NAME, new LiteralImpl(
//                                                 interactionTypeName)));
//                                             return statements;
//                                           }
                                            };

}
