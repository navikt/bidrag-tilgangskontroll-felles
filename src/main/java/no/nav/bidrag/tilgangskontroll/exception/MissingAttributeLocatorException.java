package no.nav.bidrag.tilgangskontroll.exception;

@SuppressWarnings("serial")
public class MissingAttributeLocatorException extends RuntimeException {
    public MissingAttributeLocatorException(String message) {
        super(message);
    }
}