package no.nav.bidrag.tilgangskontroll.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.bidrag.tilgangskontroll.consumer.PipConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

}
