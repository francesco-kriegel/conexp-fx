package conexp.fx.core.builder;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openrdf.repository.Repository;
import org.ujmp.core.booleanmatrix.BooleanMatrix;

import conexp.fx.core.collections.Pair;
import conexp.fx.core.collections.setlist.SetList;
import conexp.fx.core.collections.setlist.SetLists;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.importer.CEXImporter;
import conexp.fx.core.importer.CFXImporter;
import conexp.fx.core.importer.CSVImporter;
import conexp.fx.core.importer.CXTImporter;
import conexp.fx.core.importer.RDFImporter;
import conexp.fx.core.math.BooleanMatrices;

public final class Requests {

  public static enum Metatype {
    NEW("New Context", ""),
    IMPORT("Import Context", ""),
    SCALE("Scale Context", ""),
    CONSTRUCT("Construction Context", ""),
    DL_CONTEXT("DL Context", "");

//    OTHER("Other Context", "");
    public final String title;
    public final String description;

    private Metatype(final String title, final String description) {
      this.title = title;
      this.description = description;
    }
  }

  public static enum Type {
    INDUCED_CONTEXT(
        "Induced Context",
        "An induced context from a DL interpretation.",
        Metatype.DL_CONTEXT,
        false,
        Source.NULL),
    NEW_CONTEXT(
        "New Context",
        "Creates a new empty Formal Context of desired size.",
        Metatype.NEW,
        false,
        Source.INT_INT),
    NEW_ORDER(
        "New Order (experimental)",
        "Creates a new empty Order Context of desired size.",
        Metatype.NEW,
        true,
        Source.INT),
    IMPORT_CXT_CONTEXT(
        "Local File (Burmeister Format, *.cxt)",
        "Imports a Formal Context from a *.cxt File.",
        Metatype.IMPORT,
        false,
        Source.FILE),
    IMPORT_CEX_CONTEXT(
        "Local File (Concept Explorer Format, *.cex)",
        "Imports a Formal Context from a *.cex File.",
        Metatype.IMPORT,
        false,
        Source.FILE),
    IMPORT_CFX_CONTEXT(
        "Local File (Concept Explorer FX Format, *.cfx)",
        "Imports a Formal Context from a *.cfx File.",
        Metatype.IMPORT,
        false,
        Source.FILE),
    IMPORT_CSV_CONTEXT(
        "Local File (Comma Separated Values, *.csv)",
        "Imports a Formal Context from a *.csv File.",
        Metatype.IMPORT,
        false,
        Source.FILE),
    IMPORT_SPARQL_CONTEXT(
        "SPARQL Result from an Ontology",
        "Imports a Formal Context from an Ontology.",
        Metatype.IMPORT,
        false,
        Source.SPARQL_AND_XMLURL,
        Source.SPARQL_AND_ONTOLOGYFILE,
        Source.SPARQL_AND_ONTOLOGYURL,
        Source.SPARQL_AND_ONTOLOGYREPOSITORY),
    DICHTOMIC(
        "Dichtomic Scale",
        "Creates a new formal context that is the nominal scale of the two boolean values true and false",
        Metatype.SCALE,
        true,
        Source.NULL),
    BOOLEAN("Boolean Scale", "", Metatype.SCALE, true, Source.INT_LIST, Source.STRINGS),
    NOMINAL("Nominal Scale", "", Metatype.SCALE, true, Source.INT_LIST, Source.STRINGS),
    CONTRA_NOMINAL("Contra-Nominal Scale", "", Metatype.SCALE, true, Source.INT_LIST, Source.STRINGS),
    ORDINAL("Ordinal Scale", "", Metatype.SCALE, true, Source.INT_LIST, Source.STRINGS),
    CONTRA_ORDINAL("Contra-Ordinal Scale", "", Metatype.SCALE, true, Source.INT_LIST, Source.STRINGS, Source.ORDER),
    INTER_ORDINAL("Inter-Ordinal Scale", "", Metatype.SCALE, false, Source.INT_LIST, Source.STRINGS, Source.ORDER),
    CONVEX_ORDINAL("Convex-Ordinal Scale", "", Metatype.SCALE, false, Source.INT_LIST, Source.STRINGS, Source.ORDER),
    BI_ORDINAL("Bi-Ordinal Scale", "", Metatype.SCALE, false, Source.ORDER_ORDER),
    GRID("Grid Scale", "", Metatype.SCALE, false, Source.ORDER_ORDER),
    COMPLEMENT("Complement Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT),
    DUAL("Dual Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT),
    CONTRARY("Contrary Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT),
    APPOSITION("Apposition Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    SUBPOSITION("Subposition Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    QUADPOSITION("Juxtaposition Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT_CONTEXT_CONTEXT),
    HORIZONTAL_SUM("Horizontal Sum Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    VERTICAL_SUM("Vertical Sum Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    DIRECT_SUM("Direct Sum Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    DIRECT_PRODUCT("Direct Product Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    BI_PRODUCT("Bi-Product Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    SEMI_PRODUCT("Semi-Product Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT),
    SUBSTITUTION_SUM("Substitution Sum Context", "", Metatype.CONSTRUCT, false, Source.CONTEXT_CONTEXT_OBJECT_OBJECT);

//    APPROXIMATION_CONTEXT_BY_ATTRIBUTES(
//        "Dau's Approximation Context by Attributes",
//        "",
//        Metatype.OTHER,
//        false,
//        Source.CONTEXT_DOUBLE),
//    APPROXIMATION_CONTEXT("Meschke's Approximation Context", "", Metatype.OTHER, false, Source.CONTEXT_SET_SET),
//    BINARY_RELATIONS_CONTEXT("Binary Relations Context", "", Metatype.OTHER, false, Source.INT_LIST);
    public final String title;
    public final String description;
    public final Metatype type;
    public final boolean homogen;
    public final List<Source> sources;

    private Type(
        final String title,
        final String description,
        final Metatype type,
        final boolean homogen,
        final Source... sources) {
      this.title = title;
      this.description = description;
      this.type = type;
      this.homogen = homogen;
      this.sources = Arrays.asList(sources);
    }
  }

  public static enum Source {
    NULL("No source needed"),
    INT_INT("Custom Size"),
    INT("Custom Size"),
    INT_LIST("Create from natural numbers {0, ..., n}"),
    STRINGS("Create from custom set list"),
    ORDER("Create from existing order context"),
    ORDER_ORDER("Create from two existing order contexts"),
    CONTEXT("Create from existing formal context"),
    CONTEXT_CONTEXT("Create from two existing formal contexts"),
    CONTEXT_CONTEXT_CONTEXT_CONTEXT("Create from four existing formal contexts"),
    CONTEXT_SET_SET("TODO"),
    CONTEXT_DOUBLE("Create from existing formal context"),
    CONTEXT_CONTEXT_OBJECT_OBJECT("Create from two existing formal contexts"),
    FILE("Concept Explorer File Import"),
    SPARQL_AND_XMLURL("SPARQL Import from XML Endpoint"),
    SPARQL_AND_ONTOLOGYFILE("SPARQL Import from Ontology File"),
    SPARQL_AND_ONTOLOGYURL("SPARQL Import from Ontology URL"),
    SPARQL_AND_ONTOLOGYREPOSITORY("SPARQL Import from Sesame Repository");

    public final String title;

    private Source(final String title) {
      this.title = title;
    }
  }

  public final static class New {

    public static final class NewContext extends StringRequest {

      private final int objects;
      private final int attributes;

      public NewContext(final int objects, final int attributes) {
        super(Type.NEW_CONTEXT, Source.INT_INT);
        this.objects = objects;
        this.attributes = attributes;
      }

      public final void setContent() {
        for (int row = 0; row < objects; row++)
          context.rowHeads().add("Object " + row);
        for (int column = 0; column < attributes; column++)
          context.colHeads().add("Attribute " + column);
        context.pushAllChangedEvent();
      }
    }

    public static final class NewOrder extends StringRequest {

      private final int elements;

      public NewOrder(final int elements) {
        super(Type.NEW_ORDER, Source.INT);
        this.elements = elements;
      }

      public final void setContent() {
        for (int row = 0; row < elements; row++)
          context.rowHeads().add("Element " + row);
        context.pushAllChangedEvent();
      }
    }
  }

  public static final class Import {

    public static final class ImportCFX extends FileRequest {

      public ImportCFX(final File file) {
        super(Type.IMPORT_CFX_CONTEXT, file);
      }

      public final void setContent() {
        CFXImporter.importt(context, null, file);
      }
    }

    public static final class ImportCXT extends FileRequest {

      public ImportCXT(final File file) {
        super(Type.IMPORT_CXT_CONTEXT, file);
      }

      public final void setContent() throws Exception {
        CXTImporter.read(context, file);
      }
    }

    public static final class ImportCSVB extends FileRequest {

      public ImportCSVB(final File file) {
        super(Type.IMPORT_CSV_CONTEXT, file);
      }

      @Override
      public void setContent() {
        try {
          CSVImporter.importContext(file, context, ";");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public static final class ImportCEX extends StringRequest {

      private final File file;

      public ImportCEX(final File file) {
        super(Type.IMPORT_CEX_CONTEXT, Source.FILE);
        this.file = file;
      }

      public final void setContent() {
        CEXImporter.importt(context, null, file);
      }
    }

    public static final class ImportSPARQLFromEndpoint extends StringRequest {

      private final String url;
      private final String query;

      public ImportSPARQLFromEndpoint(final String url, final String query) {
        super(Type.IMPORT_SPARQL_CONTEXT, Source.SPARQL_AND_XMLURL);
        this.url = url;
        this.query = query;
      }

      public final void setContent() {
        RDFImporter.importXML(context, url, query);
      }
    }

    public static final class ImportSPARQLFromURL extends StringRequest {

      private final String url;
      private final String query;

      public ImportSPARQLFromURL(final String url, final String query) {
        super(Type.IMPORT_SPARQL_CONTEXT, Source.SPARQL_AND_ONTOLOGYURL);
        this.url = url;
        this.query = query;
      }

      public final void setContent() {
        RDFImporter.importURL(context, url, query);
      }
    }

    public static final class ImportSPARQLFromFile extends StringRequest {

      private final File   file;
      private final String query;

      public ImportSPARQLFromFile(final File file, final String query) {
        super(Type.IMPORT_SPARQL_CONTEXT, Source.SPARQL_AND_ONTOLOGYFILE);
        this.file = file;
        this.query = query;
      }

      public final void setContent() {
        RDFImporter.importFile(context, file, query);
      }
    }

    public static final class ImportSPARQLFromRepository extends StringRequest {

      private final Repository repo;
      private final String     query;

      public ImportSPARQLFromRepository(final Repository repo, final String query) {
        super(Type.IMPORT_SPARQL_CONTEXT, Source.SPARQL_AND_ONTOLOGYREPOSITORY);
        this.repo = repo;
        this.query = query;
      }

      public final void setContent() {
        RDFImporter.importRepository(context, repo, query);
      }
    }
  }

  public final static class Scale {

    public static final class DichtomicScale extends Request<Boolean, Boolean> {

      public DichtomicScale() {
        super(Type.DICHTOMIC, Source.NULL);
      }

      public final void setContent() {
        context.setContent(SetLists.create(true, false), null, BooleanMatrices.identity(2));
      }
    }

    public static final class BooleanScaleFromInt extends Request<SetList<Integer>, SetList<Integer>> {

      private final int n;

      public BooleanScaleFromInt(final int n) {
        super(Type.BOOLEAN, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(SetLists.powerSet(SetLists.integers(n)), null, BooleanMatrices.booleann(n));
      }
    }

    public static final class BooleanScaleFromSetList<E> extends Request<SetList<E>, SetList<E>> {

      private final SetList<E> s;

      public BooleanScaleFromSetList(final SetList<E> s) {
        super(Type.BOOLEAN, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(SetLists.powerSet(s), null, BooleanMatrices.booleann(s.size()));
      }
    }

    public static final class NominalScaleFromInt extends Request<Integer, Integer> {

      private final int n;

      public NominalScaleFromInt(final int n) {
        super(Type.NOMINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(SetLists.integers(n), null, BooleanMatrices.identity(n));
      }
    }

    public static final class NominalScaleFromSetList<E> extends Request<E, E> {

      private final SetList<E> s;

      public NominalScaleFromSetList(final SetList<E> s) {
        super(Type.NOMINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(s, null, BooleanMatrices.identity(s.size()));
      }
    }

    public static final class ContraNominalScaleFromInt extends Request<Integer, Integer> {

      private final int n;

      public ContraNominalScaleFromInt(final int n) {
        super(Type.CONTRA_NOMINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(SetLists.integers(n), null, BooleanMatrices.negativeIdentity(n));
      }
    }

    public static final class ContraNominalScaleFromSetList<E> extends Request<E, E> {

      private final SetList<E> s;

      public ContraNominalScaleFromSetList(final SetList<E> s) {
        super(Type.CONTRA_NOMINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(s, null, BooleanMatrices.negativeIdentity(s.size()));
      }
    }

    public static final class OrdinalScaleFromInt extends Request<Integer, Integer> {

      private final int n;

      public OrdinalScaleFromInt(final int n) {
        super(Type.ORDINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(SetLists.integers(n), null, BooleanMatrices.upperDiagonal(n));
      }
    }

    public static final class OrdinalScaleFromSetList<E> extends Request<E, E> {

      private final SetList<E> s;

      public OrdinalScaleFromSetList(final SetList<E> s) {
        super(Type.ORDINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(s, null, BooleanMatrices.upperDiagonal(s.size()));
      }
    }

    public static final class ContraOrdinalScaleFromInt extends Request<Integer, Integer> {

      private final int n;

      public ContraOrdinalScaleFromInt(final int n) {
        super(Type.CONTRA_ORDINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(
            SetLists.integers(n),
            null,
            BooleanMatrices.complement(BooleanMatrices.dual(BooleanMatrices.upperDiagonal(n))));
      }
    }

    public static final class ContraOrdinalScaleFromSetList<E> extends Request<E, E> {

      private final SetList<E> s;

      public ContraOrdinalScaleFromSetList(final SetList<E> s) {
        super(Type.CONTRA_ORDINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(
            s,
            null,
            BooleanMatrices.complement(BooleanMatrices.dual(BooleanMatrices.upperDiagonal(s.size()))));
      }
    }

    public static final class ContraOrdinalScaleFromOrder<E> extends Request<E, E> {

      private final MatrixContext<E, E> c;

      public ContraOrdinalScaleFromOrder(final MatrixContext<E, E> c) {
        super(Type.CONTRA_ORDINAL, Source.ORDER);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(c.rowHeads(), null, BooleanMatrices.complement(BooleanMatrices.dual(c.matrix())));
      }
    }

    public static final class InterOrdinalScaleFromInt extends Request<Integer, Pair<Integer, Integer>> {

      private final int n;

      public InterOrdinalScaleFromInt(final int n) {
        super(Type.INTER_ORDINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(
            SetLists.integers(n),
            SetLists.disjointUnion(SetLists.integers(n), SetLists.integers(n)),
            BooleanMatrices
                .apposition(BooleanMatrices.upperDiagonal(n), BooleanMatrices.dual(BooleanMatrices.upperDiagonal(n))));
      }
    }

    public static final class InterOrdinalScaleFromSetList<E> extends Request<E, Pair<E, E>> {

      private final SetList<E> s;

      public InterOrdinalScaleFromSetList(final SetList<E> s) {
        super(Type.INTER_ORDINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(
            s,
            SetLists.disjointUnion(s, s),
            BooleanMatrices.apposition(
                BooleanMatrices.upperDiagonal(s.size()),
                BooleanMatrices.dual(BooleanMatrices.upperDiagonal(s.size()))));
      }
    }

    public static final class InterOrdinalScaleFromOrder<E> extends Request<E, Pair<E, E>> {

      private final MatrixContext<E, E> c;

      public InterOrdinalScaleFromOrder(final MatrixContext<E, E> c) {
        super(Type.INTER_ORDINAL, Source.ORDER);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(
            c.rowHeads(),
            SetLists.disjointUnion(c.colHeads(), c.colHeads()),
            BooleanMatrices.apposition(c.matrix(), BooleanMatrices.dual(c.matrix())));
      }
    }

    public static final class ConvexOrdinalScaleFromInt extends Request<Integer, Pair<Integer, Integer>> {

      private final int n;

      public ConvexOrdinalScaleFromInt(final int n) {
        super(Type.CONVEX_ORDINAL, Source.INT_LIST);
        this.n = n;
      }

      public final void setContent() {
        context.setContent(
            SetLists.integers(n),
            SetLists.disjointUnion(SetLists.integers(n), SetLists.integers(n)),
            BooleanMatrices.apposition(
                BooleanMatrices.complement(BooleanMatrices.dual(BooleanMatrices.upperDiagonal(n))),
                BooleanMatrices.complement(BooleanMatrices.upperDiagonal(n))));
      }
    }

    public static final class ConvexOrdinalScaleFromSetList<E> extends Request<E, Pair<E, E>> {

      private final SetList<E> s;

      public ConvexOrdinalScaleFromSetList(final SetList<E> s) {
        super(Type.CONVEX_ORDINAL, Source.STRINGS);
        this.s = s;
      }

      public final void setContent() {
        context.setContent(
            s,
            SetLists.disjointUnion(s, s),
            BooleanMatrices.apposition(
                BooleanMatrices.complement(BooleanMatrices.dual(BooleanMatrices.upperDiagonal(s.size()))),
                BooleanMatrices.complement(BooleanMatrices.upperDiagonal(s.size()))));
      }
    }

    public static final class ConvexOrdinalScaleFromOrder<E> extends Request<E, Pair<E, E>> {

      private final MatrixContext<E, E> c;

      public ConvexOrdinalScaleFromOrder(final MatrixContext<E, E> c) {
        super(Type.CONVEX_ORDINAL, Source.ORDER);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(
            c.rowHeads(),
            SetLists.disjointUnion(c.colHeads(), c.colHeads()),
            BooleanMatrices.apposition(
                BooleanMatrices.complement(BooleanMatrices.dual(c.matrix())),
                BooleanMatrices.complement(c.matrix())));
      }
    }

    public static final class BiOrdinalScale<E, T> extends Request<Pair<E, T>, Pair<E, T>> {

      private final MatrixContext<E, E> order1;
      private final MatrixContext<T, T> order2;

      public BiOrdinalScale(final MatrixContext<E, E> order1, final MatrixContext<T, T> order2) {
        super(Type.BI_ORDINAL, Source.ORDER_ORDER);
        this.order1 = order1;
        this.order2 = order2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(order1.rowHeads(), order2.rowHeads()),
            SetLists.disjointUnion(order1.colHeads(), order2.colHeads()),
            BooleanMatrices.horizontalSum(order1.matrix(), order2.matrix()));
      }
    }

    public static final class GridScale<E, T> extends Request<Pair<E, T>, Pair<E, T>> {

      private final MatrixContext<E, E> order1;
      private final MatrixContext<T, T> order2;

      public GridScale(final MatrixContext<E, E> order1, final MatrixContext<T, T> order2) {
        super(Type.GRID, Source.ORDER_ORDER);
        this.order1 = order1;
        this.order2 = order2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.cartesianProduct(order1.rowHeads(), order2.rowHeads()),
            SetLists.disjointUnion(order1.colHeads(), order2.colHeads()),
            BooleanMatrices.semiProduct(order1.matrix(), order2.matrix()));
      }
    }
  }

  public static final class Construct {

    public static final class Complement<G, M> extends Request<G, M> {

      private final MatrixContext<G, M> c;

      public Complement(final MatrixContext<G, M> c) {
        super(Type.COMPLEMENT, Source.CONTEXT);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(c.rowHeads(), c.colHeads(), BooleanMatrices.complement(c.matrix()));
      }
    }

    public static final class Dual<G, M> extends Request<M, G> {

      private final MatrixContext<G, M> c;

      public Dual(final MatrixContext<G, M> c) {
        super(Type.DUAL, Source.CONTEXT);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(c.colHeads(), c.rowHeads(), BooleanMatrices.dual(c.matrix()));
      }
    }

    public static final class Contrary<G, M> extends Request<M, G> {

      private final MatrixContext<G, M> c;

      public Contrary(final MatrixContext<G, M> c) {
        super(Type.CONTRARY, Source.CONTEXT);
        this.c = c;
      }

      public final void setContent() {
        context.setContent(c.colHeads(), c.rowHeads(), BooleanMatrices.complement(BooleanMatrices.dual(c.matrix())));
      }
    }

    public static final class Apposition<G, M, N> extends Request<G, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<G, N> context2;

      public Apposition(final MatrixContext<G, M> context1, final MatrixContext<G, N> context2) {
        super(Type.APPOSITION, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.intersection(context1.rowHeads(), context2.rowHeads()),
            SetLists.disjointUnion(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.apposition(context1.matrix(), context2.matrix()));
      }
    }

    public static final class Subposition<G, H, M> extends Request<Pair<G, H>, M> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, M> context2;

      public Subposition(final MatrixContext<G, M> context1, final MatrixContext<H, M> context2) {
        super(Type.APPOSITION, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(context1.rowHeads(), context2.rowHeads()),
            SetLists.intersection(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.subposition(context1.matrix(), context2.matrix()));
      }
    }

    public static final class Quadposition<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> upperLeft;
      private final MatrixContext<G, N> upperRight;
      private final MatrixContext<H, M> lowerLeft;
      private final MatrixContext<H, N> lowerRight;

      public Quadposition(
          final MatrixContext<G, M> upperLeft,
          final MatrixContext<G, N> upperRight,
          final MatrixContext<H, M> lowerLeft,
          final MatrixContext<H, N> lowerRight) {
        super(Type.QUADPOSITION, Source.CONTEXT_CONTEXT_CONTEXT_CONTEXT);
        this.upperLeft = upperLeft;
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(
                SetLists.intersection(upperLeft.rowHeads(), upperRight.rowHeads()),
                SetLists.intersection(lowerLeft.rowHeads(), lowerRight.rowHeads())),
            SetLists.intersection(
                SetLists.disjointUnion(upperLeft.colHeads(), upperRight.colHeads()),
                SetLists.disjointUnion(lowerLeft.colHeads(), lowerRight.colHeads())),
            BooleanMatrices.subposition(
                BooleanMatrices.apposition(upperLeft.matrix(), upperRight.matrix()),
                BooleanMatrices.apposition(lowerLeft.matrix(), lowerRight.matrix())));
      }
    }

    public static final class HorizontalSum<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public HorizontalSum(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.HORIZONTAL_SUM, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(context1.rowHeads(), context2.rowHeads()),
            SetLists.disjointUnion(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.horizontalSum(context1.matrix(), context2.matrix()));
      }
    }

    public static final class VerticalSum<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public VerticalSum(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.VERTICAL_SUM, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(context1.rowHeads(), context2.rowHeads()),
            SetLists.disjointUnion(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.verticalSum(context1.matrix(), context2.matrix()));
      }
    }

    public static final class DirectSum<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public DirectSum(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.DIRECT_SUM, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.disjointUnion(context1.rowHeads(), context2.rowHeads()),
            SetLists.disjointUnion(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.directSum(context1.matrix(), context2.matrix()));
      }
    }

    public static final class DirectProduct<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public DirectProduct(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.DIRECT_PRODUCT, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.cartesianProduct(context1.rowHeads(), context2.rowHeads()),
            SetLists.cartesianProduct(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.directProduct(context1.matrix(), context2.matrix()));
      }
    }

    public static final class BiProduct<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public BiProduct(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.BI_PRODUCT, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.cartesianProduct(context1.rowHeads(), context2.rowHeads()),
            SetLists.cartesianProduct(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.biProduct(context1.matrix(), context2.matrix()));
      }
    }

    public static final class SemiProduct<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;

      public SemiProduct(final MatrixContext<G, M> context1, final MatrixContext<H, N> context2) {
        super(Type.SEMI_PRODUCT, Source.CONTEXT_CONTEXT);
        this.context1 = context1;
        this.context2 = context2;
      }

      public final void setContent() {
        context.setContent(
            SetLists.cartesianProduct(context1.rowHeads(), context2.rowHeads()),
            SetLists.disjointUnion(context1.colHeads(), context2.colHeads()),
            BooleanMatrices.semiProduct(context1.matrix(), context2.matrix()));
      }
    }

    public static final class SubstitutionSum<G, H, M, N> extends Request<Pair<G, H>, Pair<M, N>> {

      private final MatrixContext<G, M> context1;
      private final MatrixContext<H, N> context2;
      private final G                   object;
      private final M                   attribute;

      public SubstitutionSum(
          final MatrixContext<G, M> context1,
          final MatrixContext<H, N> context2,
          final G object,
          final M attribute) {
        super(Type.SUBSTITUTION_SUM, Source.CONTEXT_CONTEXT_OBJECT_OBJECT);
        this.context1 = context1;
        this.context2 = context2;
        this.object = object;
        this.attribute = attribute;
      }

      public final void setContent() {
        System.out.println("substitution sum");
        final int i = context1.rowHeads().indexOf(object);
        final int j = context1.colHeads().indexOf(attribute);
        System.out.println(i + ":" + j);
        final BooleanMatrix matrix = BooleanMatrices
            .substitutionSum(context1.matrix(), context2.matrix(), i, j, context1._row(i), context1._col(j));
        System.out.println(matrix);
        context.setContent(
            SetLists.disjointUnion(
                SetLists.difference(context1.rowHeads(), Collections.singleton(object)),
                context2.rowHeads()),
            SetLists.disjointUnion(
                SetLists.difference(context1.colHeads(), Collections.singleton(attribute)),
                context2.colHeads()),
            matrix);
        System.out.println(context);
      }
    }
  }
//
//  public static final class Other
//  {
//    public static final class AttributeApproximation<G, M>
//      extends Request<G, M>
//    {
//      private final MatrixContext<G, M> c;
//      private final double              tolerance;
//
//      public AttributeApproximation(final MatrixContext<G, M> c, final double tolerance)
//      {
//        super(Type.APPROXIMATION_CONTEXT_BY_ATTRIBUTES, Source.CONTEXT_DOUBLE);
//        this.c = c;
//        this.tolerance = tolerance;
//      }
//
//      public final void setContent()
//      {
//        final MatrixContext<G, M> approx = new AbstractContext<G, M>(c.rowHeads(), c.colHeads(), false)
//          {
//            public final boolean contains(final Object object, final Object attribute)
//            {
//              if (c.contains(object, attribute))
//                return true;
//              final Collection<Integer> attributeIntent = c._intent(c.colHeads().indexOf(attribute));
//              final Collection<Integer> objectIntent = c._row(c.rowHeads().indexOf(object));
//              final double tau =
//                  ((double) Collections3.intersection(attributeIntent, objectIntent).size())
//                      / ((double) attributeIntent.size());
//              return tau >= tolerance;
//            }
//          }.clone();
//        final BooleanMatrix matrix = approx.matrix();
//        System.out.println(matrix);
//        context.setContent(approx.rowHeads(), approx.colHeads(), matrix);
//      }
//    }
//
//    public static final class Approximation<G, M>
//      extends Request<Pair<G, G>, Pair<M, M>>
//    {
//      private final MatrixContext<G, M> c;
//      private final Set<G>              objects;
//      private final Set<M>              attributes;
//
//      public Approximation(final MatrixContext<G, M> c, final Set<G> objects, final Set<M> attributes)
//      {
//        super(Type.APPROXIMATION_CONTEXT, Source.CONTEXT_SET_SET);
//        this.c = c;
//        this.objects = objects;
//        this.attributes = attributes;
//      }
//
//      public final void setContent()
//      {
//        final MatrixContext<Pair<G, G>, Pair<M, M>> approx = ApproximationBuilder.build(c, objects, attributes);
//        context.setContent(approx.rowHeads(), approx.colHeads(), approx.matrix());
//      }
//    }
//
//    private static final class ApproximationBuilder<G, M>
//    {
//      private static final <G, M> MatrixContext<Pair<G, G>, Pair<M, M>> build(
//          final MatrixContext<G, M> context,
//          final Set<G> H,
//          final Set<M> N)
//      {
//        final MatrixContext<G, M> HM = context.subRelation(H, context.colHeads()).clone();
//        final MatrixContext<G, M> HN = context.subRelation(H, N).clone();
//        final MatrixContext<G, M> GN = context.subRelation(context.rowHeads(), N).clone();
//        final MatrixContext<G, M> GM = bondClosure(context, GN, HM);
//        final Quadposition<G, G, M, M> quadposition = new Quadposition<G, G, M, M>(HM, HN, GM, GN);
//        final MatrixContext<Pair<G, G>, Pair<M, M>> approx = quadposition.createContext();
//        quadposition.setContent();
//        return approx;
//      }
//
//      private static final <G, M> MatrixContext<G, M> bondClosure(
//          final MatrixContext<G, M> context,
//          final MatrixContext<G, M> leftContext,
//          final MatrixContext<G, M> upperContext)
//      {
//        final MatrixContext<G, M> bond = context.clone();
//        while (!isBond(bond, leftContext, upperContext)) {
//          for (G g : bond.rowHeads()) {
//            final Set<M> gBond = context.rowAnd(g);
//            final Set<M> intent = upperContext.intent(gBond);
//            for (M m : intent)
//              bond.addFast(g, m);
//          }
//          for (M m : bond.colHeads()) {
//            final Set<G> mBond = context.colAnd(m);
//            final Set<G> extent = leftContext.extent(mBond);
//            for (G g : extent)
//              bond.addFast(g, m);
//          }
//        }
//        return bond;
//      }
//
//      private static final <G, M> boolean isBond(
//          final MatrixContext<G, M> context,
//          final MatrixContext<G, M> leftContext,
//          final MatrixContext<G, M> upperContext)
//      {
//        for (G g : context.rowHeads()) {
//          final Set<M> gBond = context.rowAnd(g);
//          if (!upperContext.intent(gBond).equals(gBond))
//            return false;
//        }
//        for (M m : context.colHeads()) {
//          final Set<G> mBond = context.colAnd(m);
//          if (!leftContext.extent(mBond).equals(mBond))
//            return false;
//        }
//        return true;
//      }
//    }
//
//    public static final class BinaryRelationsFromInt
//      extends Request<MatrixRelation<Integer, Integer>, String>
//    {
//      private final int n;
//
//      public BinaryRelationsFromInt(final int n)
//      {
//        super(Type.BINARY_RELATIONS_CONTEXT, Source.INT_LIST);
//        this.n = n;
//      }
//
//      public final void setContent()
//      {
//        BinaryRelationsBuilder.build(context, n);
//      }
//    }
//
//    private static final class BinaryRelationsBuilder
//    {
//      private static final MatrixContext<MatrixRelation<Integer, Integer>, String> build(
//          final MatrixContext<MatrixRelation<Integer, Integer>, String> context,
//          final int maxSize)
//      {
//        generateAllBinaryRelations(context, maxSize);
//        context.colHeads().add("reflexive");
//        context.colHeads().add("irreflexive");
//        context.colHeads().add("symmetric");
//        context.colHeads().add("asymmetric");
//        context.colHeads().add("connex");
//        context.colHeads().add("antisymmetric");
//        context.colHeads().add("quasiconnex");
//        context.colHeads().add("alternative");
//        context.colHeads().add("transitive");
//        context.colHeads().add("negative transitive");
//        context.colHeads().add("atransitive");
//        context.colHeads().add("negative atransitive");
//        // context.getCodomain().add("0-cyclic");
//        // context.getCodomain().add("1-cyclic");
//        // context.getCodomain().add("2-cyclic");
//        // context.getCodomain().add("3-cyclic");
//        // context.getCodomain().add("4-cyclic");
//        context.colHeads().add("cyclic");
//        // context.getCodomain().add("0-acyclic");
//        // context.getCodomain().add("1-acyclic");
//        // context.getCodomain().add("2-acyclic");
//        // context.getCodomain().add("3-acyclic");
//        // context.getCodomain().add("4-acyclic");
//        context.colHeads().add("acyclic");
//        // context.getCodomain().add("0-transitive");
//        // context.getCodomain().add("1-transitive");
//        // context.getCodomain().add("2-transitive");
//        // context.getCodomain().add("3-transitive");
//        // context.getCodomain().add("4-transitive");
//        // context.getCodomain().add("0-atransitive");
//        // context.getCodomain().add("1-atransitive");
//        // context.getCodomain().add("2-atransitive");
//        // context.getCodomain().add("3-atransitive");
//        // context.getCodomain().add("4-atransitive");
//        // context.getCodomain().add("left comparative");
//        // context.getCodomain().add("right comparative");
//        for (MatrixRelation<Integer, Integer> binaryRelation : context.rowHeads()) {
//          if (binaryRelation.isReflexive())
//            context.addFast(binaryRelation, "reflexive");
//          if (binaryRelation.isIrreflexive())
//            context.addFast(binaryRelation, "irreflexive");
//          if (binaryRelation.isSymmetric())
//            context.addFast(binaryRelation, "symmetric");
//          if (binaryRelation.isAsymmetric())
//            context.addFast(binaryRelation, "asymmetric");
//          if (binaryRelation.isConnex())
//            context.addFast(binaryRelation, "connex");
//          if (binaryRelation.isAntisymmetric())
//            context.addFast(binaryRelation, "antisymmetric");
//          if (binaryRelation.isQuasiconnex())
//            context.addFast(binaryRelation, "quasiconnex");
//          if (binaryRelation.isAlternative())
//            context.addFast(binaryRelation, "alternative");
//          if (binaryRelation.isTransitive())
//            context.addFast(binaryRelation, "transitive");
//          if (binaryRelation.isNegativeTransitive())
//            context.addFast(binaryRelation, "negative transitive");
//          if (binaryRelation.isAtransitive())
//            context.addFast(binaryRelation, "atransitive");
//          if (binaryRelation.isNegativAtransitive())
//            context.addFast(binaryRelation, "negative atransitive");
//          // if(rel.isNCyclic(0)) context.set(binaryRelation, "");
//          // if(rel.isNCyclic(1)) context.set(binaryRelation, "");
//          // if(rel.isNCyclic(2)) context.set(binaryRelation, "");
//          // if(rel.isNCyclic(3)) context.set(binaryRelation, "");
//          // if(rel.isNCyclic(4)) context.set(binaryRelation, "");
//          if (binaryRelation.isCyclic())
//            context.addFast(binaryRelation, "cyclic");
//          // if(rel.isNAcyclic(0)) context.set(binaryRelation, "");
//          // if(rel.isNAcyclic(1)) context.set(binaryRelation, "");
//          // if(rel.isNAcyclic(2)) context.set(binaryRelation, "");
//          // if(rel.isNAcyclic(3)) context.set(binaryRelation, "");
//          // if(rel.isNAcyclic(4)) context.set(binaryRelation, "");
//          if (binaryRelation.isAcyclic())
//            context.addFast(binaryRelation, "acyclic");
//          // if(rel.isNTransitive(0)) context.set(binaryRelation, "");
//          // if(rel.isNTransitive(1)) context.set(binaryRelation, "");
//          // if(rel.isNTransitive(2)) context.set(binaryRelation, "");
//          // if(rel.isNTransitive(3)) context.set(binaryRelation, "");
//          // if(rel.isNTransitive(4)) context.set(binaryRelation, "");
//          // if(rel.isNAtransitive(0)) context.set(binaryRelation, "");
//          // if(rel.isNAtransitive(1)) context.set(binaryRelation, "");
//          // if(rel.isNAtransitive(2)) context.set(binaryRelation, "");
//          // if(rel.isNAtransitive(3)) context.set(binaryRelation, "");
//          // if(rel.isNAtransitive(4)) context.set(binaryRelation, "");
//          // if(rel.isLeftComparative()) context.set(binaryRelation, "");
//          // if(rel.isRightComparative()) context.set(binaryRelation, "");
//        }
//        return context;
//      }
//
//      private static final void generateAllBinaryRelations(
//          final MatrixContext<MatrixRelation<Integer, Integer>, String> context,
//          final int maxSize)
//      {
//        for (int size = 0; size < maxSize; size++) {
//          final SetList<Integer> domain = SetLists.integers(size);
//          MatrixRelation<Integer, Integer> binaryRelation;
//          for (List<Boolean> list : generateAllArrays(size * size)) {
//            binaryRelation = new MatrixRelation<Integer, Integer>(true)
//              {
//                public String toString()
//                {
//                  return Iterators.toString(iterator());
//                }
//              };
//            binaryRelation.rowHeads().addAll(domain);
//            populateMatrix(binaryRelation, list);
//            context.rowHeads().add(binaryRelation);
//          }
//        }
//      }
//
//      private static final Set<List<Boolean>> generateAllArrays(final int size)
//      {
//        final Set<List<Boolean>> arrays = new LinkedHashSet<List<Boolean>>();
//        if (size < 0)
//          return arrays;
//        if (size == 0)
//          arrays.add(new ArrayList<Boolean>());
//        else {
//          for (List<Boolean> array : generateAllArrays(size - 1)) {
//            final List<Boolean> newArray0 = new ArrayList<Boolean>(array);
//            newArray0.add(false);
//            arrays.add(newArray0);
//            final List<Boolean> newArray1 = new ArrayList<Boolean>(array);
//            newArray1.add(true);
//            arrays.add(newArray1);
//          }
//        }
//        return arrays;
//      }
//
//      private static final void
//          populateMatrix(final MatrixRelation<Integer, Integer> context, final List<Boolean> list)
//      {
//        int size = (int) Math.sqrt(list.size());
//        int row = 0;
//        int col = 0;
//        final Iterator<Boolean> iterator = list.iterator();
//        while (iterator.hasNext()) {
//          if (iterator.next())
//            context.addFast(row, col);
//          if (col < size - 1) {
//            col++;
//          } else {
//            col = 0;
//            row++;
//          }
//        }
//      }
//    }
//  }
}
