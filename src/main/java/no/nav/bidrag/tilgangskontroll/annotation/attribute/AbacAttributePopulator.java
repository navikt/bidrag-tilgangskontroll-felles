package no.nav.bidrag.tilgangskontroll.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;

public interface AbacAttributePopulator {

    void populate(XacmlRequest request, Abac abac);
}