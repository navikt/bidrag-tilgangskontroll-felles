package no.nav.bidrag.tilgangskontroll.felles.consumer;

import no.nav.bidrag.tilgangskontroll.felles.dto.PipIntern;
import no.nav.bidrag.tilgangskontroll.felles.exception.PipConsumerException;
import no.nav.bidrag.tilgangskontroll.felles.exception.SakIkkeFunnetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class PipConsumer {

  private static final String PIP_CONTEXT_PATH = "/pip/sak/";
  private static final Logger LOGGER = LoggerFactory.getLogger(PipConsumer.class);
  private final RestTemplate restTemplatePip;

  public PipConsumer(@Qualifier("pip") RestTemplate restTemplatePip) {
    this.restTemplatePip = restTemplatePip;
  }

  public PipIntern getMetaDataPip(String saksnr) {

    var path = UriComponentsBuilder.fromPath(PIP_CONTEXT_PATH + saksnr).toUriString();
    try {
      var response = restTemplatePip.exchange(path, HttpMethod.GET, null, PipIntern.class);
      LOGGER.info("Kall mot PIP returnerte med HTTP-status {} for saksnr {}", response.getStatusCode(), saksnr);
      return response.getBody();
    } catch (HttpClientErrorException hcee) {
      if (hcee.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        throw new SakIkkeFunnetException(hcee);
      }
    } catch (RestClientException rce) {
      throw new PipConsumerException(rce);
    }
    throw new PipConsumerException("Kall mot PIP feilet");
  }
}
