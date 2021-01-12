package no.nav.bidrag.tilgangskontroll.consumer;

import no.nav.bidrag.tilgangskontroll.dto.PipIntern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class PipConsumer {

  private static final String PIP_CONTEXT_PATH = "/pip/sak/";

  private final RestTemplate restTemplatePip;

  private static final Logger LOGGER = LoggerFactory.getLogger(PipConsumer.class);

  public PipConsumer(@Qualifier("pip") RestTemplate restTemplatePip) {
    this.restTemplatePip = restTemplatePip;
  }

  public PipIntern getMetaDataPip(String saksnr) {

    var path = UriComponentsBuilder.fromPath(PIP_CONTEXT_PATH + saksnr).toUriString();
    var response = restTemplatePip.exchange(path, HttpMethod.GET, null, PipIntern.class);

    LOGGER.info("Kall mot PIP returnerte med HTTP-status {} for saksnr {}", response.getStatusCode(), saksnr);

    return response.getBody();
  }
}
