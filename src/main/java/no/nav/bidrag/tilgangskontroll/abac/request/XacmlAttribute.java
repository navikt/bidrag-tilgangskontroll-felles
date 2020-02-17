package no.nav.bidrag.tilgangskontroll.abac.request;

import lombok.Value;

@Value
public class XacmlAttribute {
    private String attributeId;
    private Object value;
}