package no.nav.bidrag.tilgangskontroll.felles.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class XacmlResponse {

  private final Decision decision;
  private final Decision originalDecision;
  private final List<Obligation> obligations;
  private final List<Advice> advices;
}