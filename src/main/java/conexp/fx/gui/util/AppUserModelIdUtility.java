package conexp.fx.gui.util;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
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
