package no.nav.bidrag.tilgangskontroll.felles.strategy;

import no.nav.bidrag.tilgangskontroll.felles.response.XacmlResponse;
import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;

public interface AttributeStrategy<T> {
    boolean isSupported(String attributeId);

    void perform(T attribute, XacmlRequest request, XacmlResponse response);
}