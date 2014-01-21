package conexp.fx.core.service;

/*
 * #%L
 * Concept Explorer FX - Core
 * %%
 * Copyright (C) 2010 - 2013 TU Dresden, Chair of Automata Theory
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import conexp.fx.core.algorithm.lattice.IFox;
import conexp.fx.core.algorithm.lattice.IPred;
import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.algorithm.nextclosure.NextImplication;
import conexp.fx.core.builder.FileRequest;
import conexp.fx.core.builder.Request;
import conexp.fx.core.builder.Requests.Source;
import conexp.fx.core.concurrent.BlockingExecutor;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.context.ConceptLattice;
import conexp.fx.core.context.MatrixContext;
import conexp.fx.core.exporter.CFXExporter;
import conexp.fx.core.exporter.CXTExporter;
import conexp.fx.core.exporter.HTMLExporter;
import conexp.fx.core.exporter.PDFExporter;
import conexp.fx.core.exporter.PNGExporter;
import conexp.fx.core.exporter.SVGExporter;
import conexp.fx.core.exporter.TeXExporter;
import conexp.fx.core.layout.ConceptLayout;
import conexp.fx.core.layout.ConceptMovement;
import conexp.fx.core.layout.GeneticLayouter;
import conexp.fx.core.lock.ALock;
import conexp.fx.core.quality.ConflictDistance;
import conexp.fx.core.quality.LayoutEvolution;
import conexp.fx.core.util.Constants;
import conexp.fx.core.util.FileFormat;
import de.tudresden.inf.tcs.fcalib.Implication;

public final class FCAInstance<G, M> {

	public enum InitLevel {
		NULL(), CONCEPTS(), LATTICE(CONCEPTS), LAYOUT(CONCEPTS, LATTICE), IMPLICATIONS();

		private final Set<InitLevel> requiredLevels = new HashSet<InitLevel>();

		private InitLevel(final InitLevel... needsLevel) {
			for (InitLevel lvl : needsLevel)
				requiredLevels.add(lvl);
		}

		final boolean isOrNeedsLevel(final InitLevel lvl) {
			return this.equals(lvl) || this.requiredLevels.contains(lvl);
		}
	}

	private InitLevel defaultLvl;

	public static interface TabConfiguration {

		public Stage primaryStage();

		public ALock graphLock();

		public ALock highlightLock();

		public File lastDirectory();

		public void setLastDirectory(final File dir);

		public List<File> fileHistory();

		public boolean threeDimensions();

		public Map<Integer, Integer> rowMap();

		public Map<Integer, Integer> colMap();
	}

	final ThreadPoolExecutor tpe;
	final Request<G, M> request;
	public final MatrixContext<G, M> context;
	public final ConceptLattice<G, M> lattice;
	public final ConceptLayout<G, M> layout;
	public final ObservableList<Implication<M>> implications = FXCollections
			.<Implication<M>> observableList(new LinkedList<Implication<M>>());

	public final ConflictDistance<G, M> conflictDistance = new ConflictDistance<G, M>();
	public final BlockingExecutor executor = new BlockingExecutor();
	public final StringProperty id = new SimpleStringProperty("");
	public final BooleanProperty unsavedChanges = new SimpleBooleanProperty(
			false);
	public File file;
	public TabConfiguration tab = null;

	public FCAInstance(final FCAService service, final Request<G, M> request) {
		this.tpe = service.tpe();
		this.request = request;
		// TODO: should not be false in every case, e.g. for GUI
		this.context = request.createContext(false);
		this.context.id.bind(id);
		this.lattice = new ConceptLattice<G, M>(context);
		this.layout = new ConceptLayout<G, M>(lattice, null);
		this.id.set(request.getId());
		if (request.src != Source.FILE)
			unsavedChanges.set(true);
		else if (request instanceof FileRequest)
			file = ((FileRequest) request).file;
	}

	private final Set<FCAListener<G, M>> listeners = new HashSet<FCAListener<G, M>>();

	public final void addListener(final FCAListener<G, M> fcaListener) {
		System.out.println("added listener " + fcaListener);
		listeners.add(fcaListener);
	}

	public final void removeListener(final FCAListener<G, M> fcaListener) {
		System.out.println("removed listener " + fcaListener);
		listeners.remove(fcaListener);
	}

	public final void initialize(final InitLevel lvl) {
		this.defaultLvl = lvl;
		executor.submit(new ImportTask<G, M>(this));
		executor.submit(new InitializationTask<G, M>(this, lvl));
	}

	public final void simpleInit(final InitLevel lvl) {
		this.defaultLvl = lvl;
		executor.submit(new ImportTask<G, M>(this));
		if (lvl.isOrNeedsLevel(InitLevel.CONCEPTS)) {
			executor.submit(NextConcept.concepts(lattice));
			if (lvl.isOrNeedsLevel(InitLevel.LAYOUT))
				executor.submit(new SeedsAndLabelsTask<G, M>(this));
			if (lvl.isOrNeedsLevel(InitLevel.LATTICE))
				executor.submit(IPred.neighborhood(lattice));
			if (lvl.isOrNeedsLevel(InitLevel.LAYOUT))
				executor.submit(GeneticLayouter.seeds(layout, false,
						Constants.GENERATIONS, Constants.POPULATION,
						tab == null ? false : tab.threeDimensions(),
						conflictDistance, tpe));
		}
	}

	public final void addObject(final G object) {
		executor.submit(new BlockingTask("New Object") {

			protected final void _call() {
				updateProgress(0.5d, 1d);
				if (tab != null) {
					tab.graphLock().lock();
					tab.highlightLock().lock();
				}
				unsavedChanges.set(true);
				context.rowHeads().add(object);
				context.pushAllChangedEvent();
				lattice.rowHeads().clear();
			}
		});
		executor.submit(new InitializationTask<G, M>(this, defaultLvl));
	}

	public final void addAttribute(final M attribute) {
		executor.submit(new BlockingTask("New Attribute") {

			protected final void _call() {
				updateProgress(0.5d, 1d);
				if (tab != null) {
					tab.graphLock().lock();
					tab.highlightLock().lock();
				}
				unsavedChanges.set(true);
				context.colHeads().add(attribute);
				context.pushAllChangedEvent();
				lattice.rowHeads().clear();
			}
		});
		executor.submit(new InitializationTask<G, M>(this, defaultLvl));
	}

	public final void flip(final G object, final M attribute) {
		executor.submit(new BlockingTask("Context Flip") {

			protected final void _call() {
				updateProgress(0.5d, 1d);
				if (!context.selectedAttributes().contains(attribute)) {
					if (context.contains(object, attribute))
						context.remove(object, attribute);
					else
						context.addFast(object, attribute);
					return;
				} else {
					executor.submit(new BlockingTask("Flip Init") {

						protected final void _call() {
							updateProgress(0.5d, 1d);
							if (!context.selectedAttributes().contains(
									attribute)) {
								if (context.contains(object, attribute))
									context.remove(object, attribute);
								else
									context.addFast(object, attribute);
								return;
							}
							tab.graphLock().lock();
							tab.highlightLock().lock();
						}
					});
					executor.submit(IFox.ignore(layout, attribute,
							conflictDistance, tpe));
					executor.submit(new BlockingTask("Context Flip") {

						protected final void _call() {
							updateProgress(0.5d, 1d);
							tab.graphLock().unlock();
							tab.highlightLock().unlock();
							if (layout.lattice.context.contains(object,
									attribute))
								layout.lattice.context
										.remove(object, attribute);
							else
								layout.lattice.context.addFast(object,
										attribute);
							tab.graphLock().lock();
							tab.highlightLock().lock();
						}
					});
					executor.submit(IFox.select(layout, attribute,
							conflictDistance, tpe));
					if (tab != null)
						executor.submit(new BlockingTask("Flip Finish") {

							protected final void _call() {
								updateProgress(0.5d, 1d);
								tab.graphLock().unlock();
								tab.highlightLock().unlock();
							}
						});
				}
			}
		});
	}

	public final void select(final M attribute) {
		if (tab != null)
			executor.submit(new BlockingTask("Select Init") {

				protected final void _call() {
					updateProgress(0.5d, 1d);
					tab.graphLock().lock();
					tab.highlightLock().lock();
				}
			});
		executor.submit(IFox.select(layout, attribute, conflictDistance, tpe));
		if (tab != null)
			executor.submit(new BlockingTask("Select Finish") {

				protected final void _call() {
					updateProgress(0.5d, 1d);
					tab.graphLock().unlock();
					tab.highlightLock().unlock();
				}
			});
	}

	public final void ignore(final M attribute) {
		if (tab != null)
			executor.submit(new BlockingTask("Ignore Init") {

				protected final void _call() {
					updateProgress(0.5d, 1d);
					tab.graphLock().lock();
					tab.highlightLock().lock();
				}
			});
		executor.submit(IFox.ignore(layout, attribute, conflictDistance, tpe));
		if (tab != null)
			executor.submit(new BlockingTask("Ignore Finish") {

				protected final void _call() {
					updateProgress(0.5d, 1d);
					tab.graphLock().unlock();
					tab.highlightLock().unlock();
				}
			});
	}

	public final void relayout(final int generationCount,
			final int populationSize) {
		executor.submit(GeneticLayouter.seeds(layout, false, generationCount,
				populationSize, tab == null ? false : tab.threeDimensions(),
				conflictDistance, tpe));
	}

	public final void refine(final int generationCount) {
		executor.submit(GeneticLayouter.seeds(layout, true, generationCount, 1,
				tab == null ? false : tab.threeDimensions(), conflictDistance,
				tpe));
	}

	public final LayoutEvolution<G, M> qualityChart(
			final Concept<G, M> concept, final ConceptMovement movement) {
		final LayoutEvolution<G, M> qualityEvolution = new LayoutEvolution<G, M>(
				layout, concept, movement, 2d, 2d, 32, 1, 16, conflictDistance,
				tpe);
		executor.submit(LayoutEvolution.calculate(qualityEvolution));
		return qualityEvolution;
	}

	public final void storeToFile() {
		executor.submit(new BlockingTask("Store") {

			@SuppressWarnings("incomplete-switch")
			protected final void _call() {
				updateProgress(0.5d, 1d);
				switch (FileFormat.of(file, FileFormat.CFX, FileFormat.CXT)
						.second()) {
				case CXT:
					CXTExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), file);
					break;
				case CFX:
					CFXExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, file);
					break;
				}
				id.set(file.getName());
				unsavedChanges.set(false);
				tab.fileHistory().remove(file);
				tab.fileHistory().add(0, file);
			}
		});
	}

	public void export(FileFormat svg, File file2) {
		exportToFile(file2);
	}

	public final void exportToFile(final File file) {
		executor.submit(new BlockingTask("Export") {

			@SuppressWarnings("incomplete-switch")
			protected final void _call() {
				updateProgress(0.5d, 1d);
				switch (FileFormat.of(file, FileFormat.TEX, FileFormat.PNG,
						FileFormat.SVG, FileFormat.PDF, FileFormat.HTML)
						.second()) {
				case TEX:
					TeXExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, true,
							true, file);
					break;
				case PNG:
					PNGExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, true,
							true, file);
					break;
				case SVG:
					SVGExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, true,
							true, file);
					break;
				case PDF:
					PDFExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, true,
							true, file);
					break;
				case HTML:
					HTMLExporter.export(context,
							tab == null ? null : tab.rowMap(),
							tab == null ? null : tab.colMap(), layout, true,
							true, file);
					break;
				}
			}
		});
	}

	public void calcImplications() {
		executor.submit(new BlockingTask("Clear implications list") {

			@Override
			protected void _call() {
				implications.clear();
			}
		});
		executor.submit(NextImplication.implications(context, implications));
	}

	public void calcAssociations(final double support, final double confidence) {
		executor.submit(new BlockingTask("Association Rules") {

			@Override
			protected void _call() {
				// TODO Auto-generated method stub
				// Implement and connect Titanic algorithm
			}
		});
	}

	public final void setTabConfiguration(
			final TabConfiguration tabConfiguration) {
		this.tab = tabConfiguration;
		layout.observe();
	}
}
