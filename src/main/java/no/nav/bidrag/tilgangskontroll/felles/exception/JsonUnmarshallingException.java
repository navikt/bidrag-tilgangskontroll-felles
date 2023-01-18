package no.nav.bidrag.tilgangskontroll.felles.exception;

@SuppressWarnings("serial")
public class JsonUnmarshallingException extends RuntimeException {

  public JsonUnmarshallingException(String message, Throwable cause) {
    super(message, cause);
  }
}