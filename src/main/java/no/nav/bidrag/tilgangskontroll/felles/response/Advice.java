package no.nav.bidrag.tilgangskontroll.felles.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Advice {

  private String id;
  private List<AttributeAssignment> attributeAssignments;
}