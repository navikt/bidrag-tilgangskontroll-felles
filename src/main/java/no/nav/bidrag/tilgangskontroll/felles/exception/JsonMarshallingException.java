package no.nav.bidrag.tilgangskontroll.felles.exception;

@SuppressWarnings("serial")
public class JsonMarshallingException extends RuntimeException {

  public JsonMarshallingException(String message, Throwable cause) {
    super(message, cause);
  }
}