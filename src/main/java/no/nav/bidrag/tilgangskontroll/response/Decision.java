package no.nav.bidrag.tilgangskontroll.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Decision {
  PERMIT("Permit"),
  DENY("Deny"),
  INDETERMINATE("Indeterminate"),
  NOT_APPLICABLE("NotApplicable");

  private final String value;

  public static Decision findByValue(String value) {
    for (Decision decision : Decision.values()) {
      if (decision.getValue().equals(value)) {
        return decision;
      }
    }
    return null;
  }
}