package no.nav.bidrag.tilgangskontroll.felles.config;

import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.tilgangskontroll.felles.SecurityUtils;
import no.nav.bidrag.tilgangskontroll.felles.consumer.PipConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class AccessControlConfig {

  @Bean
  public PipConsumer pipConsumer(
      @Value("${PIP_URL}") String pipUrl,
      RestTemplate restTemplatePip
  ) {
    restTemplatePip.setUriTemplateHandler(new RootUriTemplateHandler(pipUrl));
    log.info("PipConsumer med base url: " + pipUrl);

    return new PipConsumer(restTemplatePip);
  }

  @Bean
  @Scope("prototype")
  @Qualifier("abac")
  public RestTemplate restTemplateAbac(
      @Value("${ABAC_USERNAME}") String systemuser_username,
      @Value("${ABAC_PASSWORD}") String systemuser_password
  ) {
    return getRestTemplate(systemuser_username, systemuser_password);
  }

  @Bean
  @Scope("prototype")
  @Qualifier("pip")
  public RestTemplate restTemplatePip(
      @Value("${PIP_USERNAME}") String systemuser_username,
      @Value("${PIP_PASSWORD}") String systemuser_password
  ) {
    return getRestTemplate(systemuser_username, systemuser_password);
  }

  @NotNull
  private RestTemplate getRestTemplate(String systemuser_username, String systemuser_password) {
    var httpHeaderRestTemplate = new HttpHeaderRestTemplate();

    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER, CorrelationIdFilter::fetchCorrelationIdForThread);
    httpHeaderRestTemplate.addHeaderGenerator(EnhetFilter.X_ENHET_HEADER, EnhetFilter::fetchForThread);
    httpHeaderRestTemplate.addHeaderGenerator(
        HttpHeaders.AUTHORIZATION, () -> "Basic " + SecurityUtils.base64EncodeCredentials(systemuser_username, systemuser_password)
    );

    return httpHeaderRestTemplate;
  }

}
