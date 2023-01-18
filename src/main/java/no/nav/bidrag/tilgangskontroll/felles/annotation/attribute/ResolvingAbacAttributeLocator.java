package no.nav.bidrag.tilgangskontroll.felles.annotation.attribute;

import lombok.Value;

@Value
public class ResolvingAbacAttributeLocator implements AbacAttributeLocator {

  private String attribute;
  private AttributeSupplier supplier;

  @Override
  public String getAttribute() {
    return attribute;
  }

  @Override
  public Object getValue() {
    return supplier.get();
  }
}