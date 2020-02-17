package no.nav.bidrag.tilgangskontroll.abac.strategy;

import no.nav.bidrag.tilgangskontroll.abac.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.abac.response.XacmlResponse;

public interface AttributeStrategy<T> {
    boolean isSupported(String attributeId);

    void perform(T attribute, XacmlRequest request, XacmlResponse response);
}