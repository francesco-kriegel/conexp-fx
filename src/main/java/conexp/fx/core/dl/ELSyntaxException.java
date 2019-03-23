package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
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

public class ELSyntaxException extends UnsupportedOperationException {

  private static final long   serialVersionUID = 5805063251933016214L;

  private static final String message          = "Currently only EL-concept expressions are supported.";

  public ELSyntaxException() {
    super(message);
  }

  public ELSyntaxException(final Throwable cause) {
    super(message, cause);
  }

  public ELSyntaxException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
