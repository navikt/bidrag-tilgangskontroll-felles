package no.nav.bidrag.tilgangskontroll.abac.annotation.context;

import no.nav.bidrag.tilgangskontroll.abac.request.XacmlRequest;
import org.springframework.stereotype.Component;

@Component
public interface AbacContext {

  XacmlRequest getRequest();

  void cleanUp();
}
