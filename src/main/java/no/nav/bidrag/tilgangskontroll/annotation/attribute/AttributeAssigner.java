package no.nav.bidrag.tilgangskontroll.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;

interface AttributeAssigner {
    void assign(XacmlRequest request, String id, Object value);
}