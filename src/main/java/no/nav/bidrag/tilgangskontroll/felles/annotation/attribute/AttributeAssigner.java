package no.nav.bidrag.tilgangskontroll.felles.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;

interface AttributeAssigner {

  void assign(XacmlRequest request, String id, Object value);
}