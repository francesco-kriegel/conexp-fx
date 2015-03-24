package conexp.fx.core.dl;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;

import conexp.fx.core.collections.pair.Pair;

public class ELNormalFormTest {

  @Test
  public void test() {
    final ELConceptDescription elconcept =
        new ELConceptDescription(Sets.newHashSet(IRI.create("A"), IRI.create("B"), OWLManager
            .getOWLDataFactory()
            .getOWLNothing()
            .getIRI()), Sets.newHashSet(new Pair<IRI, ELConceptDescription>(IRI.create("r"), new ELConceptDescription(Collections
            .singleton(IRI.create("C")), Collections.emptySet()))));
    final ELConceptDescription expected =
        new ELConceptDescription(Sets.newHashSet(OWLManager.getOWLDataFactory().getOWLNothing().getIRI()), Sets.newHashSet());
    Assert.assertNotEquals(elconcept, expected);
    Assert.assertEquals(elconcept.minimize(), expected);
    Assert.assertEquals(elconcept.minimize(), expected.minimize());
    Assert.assertEquals(expected.minimize(), expected);


//    final ELNormalForm el1 =
//        new ELNormalForm(Sets.newHashSet(IRI.create("A")), Sets.newHashSet(new Pair<IRI, ELNormalForm>(
//            IRI.create("r"),
//            ab1), new Pair<IRI, ELNormalForm>(IRI.create("r"), a1), new Pair<IRI, ELNormalForm>(IRI.create("r"), b1)));
//    System.out.println(el1.toOWLClassExpression());
//    System.out.println(el1.minimize().toOWLClassExpression());
  }

}
