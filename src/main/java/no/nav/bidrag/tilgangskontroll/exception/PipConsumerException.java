package no.nav.bidrag.tilgangskontroll.exception;

import org.springframework.web.client.RestClientException;

public class PipConsumerException extends RuntimeException {

  public PipConsumerException(RestClientException rce) {
    super(rce);
  }

  public PipConsumerException(String feilmelding) {
    super(feilmelding);
  }
}
