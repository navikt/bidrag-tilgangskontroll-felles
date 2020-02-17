package no.nav.bidrag.tilgangskontroll.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class SakIkkeFunnetException extends RuntimeException {

  private static final String SAK_IKKE_FUNNET = "Relatert sak ikke funnet!";

  public SakIkkeFunnetException(String message) {
    super(message);
  }

  public SakIkkeFunnetException() {
    super(SAK_IKKE_FUNNET);
  }

}
