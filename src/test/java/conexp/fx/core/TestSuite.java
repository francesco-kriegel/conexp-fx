package conexp.fx.core;

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
