package no.nav.bidrag.tilgangskontroll.felles.annotation.context;

import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;
import org.springframework.stereotype.Component;

@Component
public interface AbacContext {

  XacmlRequest getRequest();

  void cleanUp();
}
