package conexp.fx.core;

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;

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
