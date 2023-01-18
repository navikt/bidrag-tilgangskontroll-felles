package no.nav.bidrag.tilgangskontroll.felles.exception;

@SuppressWarnings("serial")
public class UnhandledObligationException extends RuntimeException {

  public UnhandledObligationException(String obligationId) {
    super("No strategy found to handle obligation with id: " + obligationId);
  }
}