package no.nav.bidrag.tilgangskontroll.felles.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;

public interface AbacAttributePopulator {

  void populate(XacmlRequest request, Abac abac);
}