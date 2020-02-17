package no.nav.bidrag.tilgangskontroll.exception;

@SuppressWarnings("serial")
public class IndeterminateDecisionException extends RuntimeException {
    public IndeterminateDecisionException() {
        super("Got indeterminate result from ABAC");
    }
}