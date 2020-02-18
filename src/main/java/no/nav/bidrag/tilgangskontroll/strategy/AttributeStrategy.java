package no.nav.bidrag.tilgangskontroll.strategy;

import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.response.XacmlResponse;

public interface AttributeStrategy<T> {
    boolean isSupported(String attributeId);

    void perform(T attribute, XacmlRequest request, XacmlResponse response);
}