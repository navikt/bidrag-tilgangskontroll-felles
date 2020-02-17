package no.nav.bidrag.tilgangskontroll.dto


import java.util.Collections.emptyList

data class BidragSakPipIntern(
    var saksnummer: String? = null,
    var erParagraf19: Boolean = false,
    var roller: List<String> = emptyList()
)