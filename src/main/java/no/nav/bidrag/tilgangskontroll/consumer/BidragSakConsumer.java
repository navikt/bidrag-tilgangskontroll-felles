package no.nav.bidrag.tilgangskontroll.consumer;

import no.nav.bidrag.tilgangskontroll.dto.BidragSakPipIntern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragSakConsumer {

  private static final String SAK_CONTEXT_PATH = "/pip/sak/";

  private final RestTemplate restTemplatePip;

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragSakConsumer.class);

  public BidragSakConsumer(RestTemplate restTemplatePip) {
    this.restTemplatePip = restTemplatePip;
  }

  public BidragSakPipIntern getMetaDataBidragsak(String saksnr) {

    var path = UriComponentsBuilder.fromPath(SAK_CONTEXT_PATH + saksnr).toUriString();
    var response = restTemplatePip.exchange(path, HttpMethod.GET, null, BidragSakPipIntern.class);

    LOGGER.info("Kall mot PIP for Bidragsak returnerte med HTTP-status {} for saksnr {}", response.getStatusCode(), saksnr);

    return response.getBody();
  }
}
