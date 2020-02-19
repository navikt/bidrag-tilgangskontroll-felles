package no.nav.bidrag.tilgangskontroll;

import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.bidrag.commons.web.EnhetFilter;
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate;
import no.nav.bidrag.tilgangskontroll.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

  @Bean
  @Scope("prototype")
  public RestTemplate restTemplate(
      @Value("${ABAC_USERNAME}") String systemuser_username,
      @Value("${ABAC_PASSWORD}") String systemuser_password
  ) {
    return getRestTemplate(systemuser_username, systemuser_password);
  }

  @Bean
  @Scope("prototype")
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
    httpHeaderRestTemplate.addHeaderGenerator(EnhetFilter.X_ENHETSNR_HEADER, EnhetFilter::fetchForThread);
    httpHeaderRestTemplate.addHeaderGenerator(
        HttpHeaders.AUTHORIZATION, () -> "Basic " + SecurityUtils.base64EncodeCredentials(systemuser_username, systemuser_password)
    );

    return httpHeaderRestTemplate;
  }

}
