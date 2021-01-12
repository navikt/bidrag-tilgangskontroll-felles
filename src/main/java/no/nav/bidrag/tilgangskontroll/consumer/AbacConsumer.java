package no.nav.bidrag.tilgangskontroll.consumer;

import static java.util.Collections.singletonList;

import java.util.List;
import no.nav.bidrag.tilgangskontroll.exception.IndeterminateDecisionException;
import no.nav.bidrag.tilgangskontroll.exception.UnexpectedHttpCodeException;
import no.nav.bidrag.tilgangskontroll.exception.UnhandledObligationException;
import no.nav.bidrag.tilgangskontroll.request.AbacRequestMapper;
import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.response.AbacResponseMapper;
import no.nav.bidrag.tilgangskontroll.response.Advice;
import no.nav.bidrag.tilgangskontroll.response.Decision;
import no.nav.bidrag.tilgangskontroll.response.Obligation;
import no.nav.bidrag.tilgangskontroll.response.XacmlResponse;
import no.nav.bidrag.tilgangskontroll.strategy.AdviceStrategy;
import no.nav.bidrag.tilgangskontroll.strategy.AttributeStrategy;
import no.nav.bidrag.tilgangskontroll.strategy.ObligationStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AbacConsumer {

  private static final MediaType APPLICATION_XACML_AND_JSON =
      MediaType.parseMediaType("application/xacml+json");

  private final List<ObligationStrategy> obligationStrategies;
  private final List<AdviceStrategy> adviceStrategies;

  private RestTemplate restTemplate;
  private String url;
  private AbacRequestMapper requestMapper;
  private AbacResponseMapper responseMapper;

  public AbacConsumer(
      List<ObligationStrategy> obligationStrategies,
      List<AdviceStrategy> adviceStrategies,
      @Qualifier("abac") RestTemplate restTemplate,
      @Value("${ABAC_PDP_ENDPOINT_URL}") String url,
      AbacRequestMapper requestMapper,
      AbacResponseMapper responseMapper) {

    this.obligationStrategies = obligationStrategies;
    this.adviceStrategies = adviceStrategies;
    this.restTemplate = restTemplate;
    this.url = url;
    this.requestMapper = requestMapper;
    this.responseMapper = responseMapper;
  }

  public XacmlResponse evaluate(XacmlRequest request) {
    HttpEntity<String> httpRequest = prepareHttpRequest(request);

    ResponseEntity<String> abacResult = restTemplate.postForEntity(url, httpRequest, String.class);

    if (abacResult.getStatusCode().value() < 200 || abacResult.getStatusCode().value() > 299) {
      throw new UnexpectedHttpCodeException(
          abacResult.getStatusCode().value(), 200, abacResult.getStatusCode().getReasonPhrase());
    }

    XacmlResponse response = responseMapper.map(abacResult.getBody());

    response = assignResultBasedOnBias(request, response);

    handleObligations(request, response);
    handleAdvice(request, response);

    return response;
  }

  private HttpEntity<String> prepareHttpRequest(XacmlRequest request) {
    String requestAsJson = requestMapper.map(request);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_XACML_AND_JSON);
    headers.setAccept(singletonList(APPLICATION_XACML_AND_JSON));
    return new HttpEntity<>(requestAsJson, headers);
  }

  private XacmlResponse assignResultBasedOnBias(XacmlRequest request, XacmlResponse response) {
    if (response.getOriginalDecision() == Decision.INDETERMINATE
        && request.isFailOnIndeterminate()) {
      throw new IndeterminateDecisionException();
    } else if (response.getOriginalDecision() != Decision.PERMIT
        && response.getOriginalDecision() != Decision.DENY) {
      return new XacmlResponse(
          request.getBias(),
          response.getOriginalDecision(),
          response.getObligations(),
          response.getAdvices());
    }
    return response;
  }

  private void handleObligations(XacmlRequest request, XacmlResponse response) {
    for (Obligation obligation : response.getObligations()) {
      ObligationStrategy strategy = findSupportedStrategy(obligation.getId(), obligationStrategies);
      if (strategy == null) {
        throw new UnhandledObligationException(obligation.getId());
      }
      strategy.perform(obligation, request, response);
    }
  }

  private void handleAdvice(XacmlRequest request, XacmlResponse response) {
    for (Advice advice : response.getAdvices()) {
      AdviceStrategy strategy = findSupportedStrategy(advice.getId(), adviceStrategies);
      if (strategy != null) {
        strategy.perform(advice, request, response);
      }
    }
  }

  private <T extends AttributeStrategy<?>> T findSupportedStrategy(String id, List<T> strategies) {
    for (T strategy : strategies) {
      if (strategy.isSupported(id)) {
        return strategy;
      }
    }
    return null;
  }
}
