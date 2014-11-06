package conexp.fx.gui.util;

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


import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;

public class AppUserModelIdUtility {

  static {
    Native.register("shell32");
  }

  private static native NativeLong GetCurrentProcessExplicitAppUserModelID(PointerByReference appID);

  private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

  // DO NOT DO THIS, IT'S JUST FOR TESTING PURPOSE AS I'M NOT FREEING THE MEMORY
  // AS REQUESTED BY THE DOCUMENTATION:
  //
  // http://msdn.microsoft.com/en-us/library/dd378419%28VS.85%29.aspx
  //
  // "The caller is responsible for freeing this string with CoTaskMemFree when
  // it is no longer needed"
  public static final String getCurrentProcessExplicitAppUserModelID() {
    final PointerByReference r = new PointerByReference();

    if (GetCurrentProcessExplicitAppUserModelID(r).longValue() == 0) {
      final Pointer p = r.getValue();

      return p.getString(0, true); // here we leak native memory by lazyness
    }
    return "N/A";
  }

  public static final void setCurrentProcessExplicitAppUserModelID(final String appID) {
    if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
      throw new RuntimeException("unable to set current process explicit AppUserModelID to: " + appID);
  }

}
