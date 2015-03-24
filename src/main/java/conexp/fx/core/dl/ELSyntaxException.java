package conexp.fx.core.dl;

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
