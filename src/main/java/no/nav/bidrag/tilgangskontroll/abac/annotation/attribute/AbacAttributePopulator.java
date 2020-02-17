package no.nav.bidrag.tilgangskontroll.abac.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.abac.request.XacmlRequest;

public interface AbacAttributePopulator {

    void populate(XacmlRequest request, Abac abac);
}