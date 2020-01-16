package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2020 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
