package no.nav.bidrag.tilgangskontroll.felles.request;

import lombok.Value;

@Value
public class XacmlAttribute {

  private String attributeId;
  private Object value;
}