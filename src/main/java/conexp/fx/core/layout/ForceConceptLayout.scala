package conexp.fx.core.layout

import conexp.fx.core.context.Concept
import conexp.fx.core.context.ConceptLattice
//import me.enkode.j8.Java8Converters._
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point3D

class ForceConceptLayout[G, M](lattice: ConceptLattice[G, M]) extends ConceptLayout[G, M, Property[Point3D]](lattice) {

  implicit def toRunnable[T](t: => T): Runnable = new Runnable { override def run() = { t } }

  val delay = 250
  val thread = new Thread(
    while (true) {
      updatePositions
      Thread sleep delay
    })
  thread.run()

  def updatePositions() {

  }

  def deleteZ() {
//    positionBindings.forEach((x: Concept[G, M], y: Property[Point3D]) => y.setValue(y.getValue().subtract(0, 0, 0)))
  }
  def rotate(angle: Double) {}
  def newPositionBinding(concept: Concept[G, M]) = new SimpleObjectProperty[Point3D]
  def move(concept: Concept[G, M], movement: ConceptMovement, delta: Point3D) {}

}