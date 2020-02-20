package no.nav.bidrag.tilgangskontroll.annotation.context;

import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import org.springframework.stereotype.Component;

@Component
public interface AbacContext {

  XacmlRequest getRequest();

  void cleanUp();
}
