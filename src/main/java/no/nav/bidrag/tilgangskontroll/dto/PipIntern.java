package no.nav.bidrag.tilgangskontroll.dto;

import static java.util.Collections.emptyList;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PipIntern {
  private String saksnummer;
  private Boolean erParagraf19 = false;
  private List<String> roller = emptyList();
}
