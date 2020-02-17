package no.nav.bidrag.tilgangskontroll.abac.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.abac.request.XacmlRequest;

interface AttributeAssigner {
    void assign(XacmlRequest request, String id, Object value);
}