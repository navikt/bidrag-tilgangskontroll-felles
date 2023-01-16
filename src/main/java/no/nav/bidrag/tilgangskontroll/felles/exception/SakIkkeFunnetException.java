package no.nav.bidrag.tilgangskontroll.felles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SakIkkeFunnetException extends PipConsumerException {

  public SakIkkeFunnetException(RestClientException rce) {
    super(rce);
  }

  public SakIkkeFunnetException(String feilmelding) {
    super(feilmelding);
  }

}
