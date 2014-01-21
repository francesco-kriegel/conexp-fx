package conexp.fx.core.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;

import conexp.fx.core.algorithm.nextclosure.NextConceptTest;

public final class TestSuite {

  public static final void main(final String... args) {
    new TestSuite().run();
  }

  private final class JUnitTestListener extends RunListener {}

  public TestSuite() {
    super();
  }

  public final void run() {
    final JUnitCore jUnitCore = new JUnitCore();
    jUnitCore.addListener(new JUnitTestListener());
    jUnitCore.run(TestContexts.class);
    jUnitCore.run(NextConceptTest.class);
//    jUnitCore.run(NextImplicationTest.class);
//    jUnitCore.run(NextClosureTest.class);
  }

}