package no.nav.bidrag.tilgangskontroll.exception;

@SuppressWarnings("serial")
public class JsonMarshallingException extends RuntimeException {
    public JsonMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }
}