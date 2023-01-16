package no.nav.bidrag.tilgangskontroll.felles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SecurityConstraintException extends RuntimeException {

  private static final long serialVersionUID = 10892516L;

  public SecurityConstraintException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public SecurityConstraintException(String message) {
    super(message);
  }

  public SecurityConstraintException(Throwable throwable) {
    super(throwable);
  }

}
