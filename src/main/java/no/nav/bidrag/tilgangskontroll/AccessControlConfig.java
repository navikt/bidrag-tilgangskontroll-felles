package no.nav.bidrag.tilgangskontroll.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.bidrag.tilgangskontroll.AccessControlService;
import no.nav.bidrag.tilgangskontroll.annotation.EnableAccessControl;
import no.nav.bidrag.tilgangskontroll.consumer.PipConsumer;
import no.nav.bidrag.tilgangskontroll.interceptor.AccessControlClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class AccessControlConfig implements WebMvcConfigurer, ImportAware {

  private AnnotationAttributes enableAccessControlAnnotationAttributes;

  @Autowired
  private AccessControlService accessControlService;

  @Bean
  public AccessControlClientHttpRequestInterceptor accessControlClientHttpRequestInterceptor(){
    log.info("creating bean for ClientHttpRequestInterceptor");
    return new AccessControlClientHttpRequestInterceptor(enableAccessControlAnnotationAttributes, accessControlService);
  }

  @Bean
  public PipConsumer pipConsumer(
      @Value("${PIP_URL}") String pipUrl,
      RestTemplate restTemplatePip
  ) {
    restTemplatePip.setUriTemplateHandler(new RootUriTemplateHandler(pipUrl));
    log.info("PipConsumer med base url: " + pipUrl);

    return new PipConsumer(restTemplatePip);
  }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
      this.enableAccessControlAnnotationAttributes = AnnotationAttributes.fromMap(
          importMetadata.getAnnotationAttributes(EnableAccessControl.class.getName(), false));
      if (this.enableAccessControlAnnotationAttributes == null) {
        throw new IllegalArgumentException(
            "@EnableAccessControl is not present on importing class " + importMetadata.getClassName());
      }
  }
}
