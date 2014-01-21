package conexp.fx.core.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javafx.geometry.Point3D;

import org.ujmp.core.util.RandomSimple;

import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;

final class SeedsAndLabelsTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCAInstance<G, M> fcaInstance;

  public SeedsAndLabelsTask(FCAInstance<G, M> fcaInstance) {
    super("Initial Seeds and Labels");
    this.fcaInstance = fcaInstance;
  }

  protected final void _call() {
    final Random rng = new RandomSimple();
    updateMessage("Computing Infimum Irreducibles...");
    final Set<M> infimumIrreducibles = this.fcaInstance.layout.lattice.context.infimumIrreducibles();
    updateProgress(0.2d, 1d);
    updateProgress(0.3d, 1d);
    updateMessage("Generating Layered Random Seeds...");
    final Map<M, Point3D> randomSeeds = new HashMap<M, Point3D>();
    for (M m : infimumIrreducibles)
      randomSeeds.put(m, new Point3D(2d * rng.nextDouble() - 1d, 1, 0));
    updateProgress(0.4d, 1d);
    this.fcaInstance.layout.updateSeeds(randomSeeds);
    updateProgress(0.5d, 1d);
    updateMessage("Computing Attribute Labels...");
    for (Concept<G, M> c : this.fcaInstance.layout.lattice.rowHeads()) {
      final Set<M> attributeLabels = new HashSet<M>(this.fcaInstance.layout.lattice.context.attributeLabels(c.extent(), c.intent()));
      synchronized (this.fcaInstance.layout.lattice.attributeConcepts) {
        for (M m : attributeLabels)
          this.fcaInstance.layout.lattice.attributeConcepts.put(m, c);
      }
    }
    updateProgress(0.75d, 1d);
    updateMessage("Computing Object Labels...");
    for (Concept<G, M> c : this.fcaInstance.layout.lattice.rowHeads()) {
      final Set<G> objectLabels = new HashSet<G>(this.fcaInstance.layout.lattice.context.objectLabels(c.extent(), c.intent()));
      synchronized (this.fcaInstance.layout.lattice.objectConcepts) {
        for (G g : objectLabels)
          this.fcaInstance.layout.lattice.objectConcepts.put(g, c);
      }
    }
  }
}