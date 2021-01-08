package no.nav.bidrag.tilgangskontroll.service;

import static no.nav.bidrag.tilgangskontroll.SecurityUtils.parseIdToken;
import static no.nav.bidrag.tilgangskontroll.config.StandardAttributter.ACTION_ID;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.bidrag.tilgangskontroll.annotation.attribute.Abac;
import no.nav.bidrag.tilgangskontroll.annotation.context.AbacContext;
import no.nav.bidrag.tilgangskontroll.config.NavAttributter;
import no.nav.bidrag.tilgangskontroll.consumer.AbacConsumer;
import no.nav.bidrag.tilgangskontroll.consumer.PipConsumer;
import no.nav.bidrag.tilgangskontroll.dto.PipIntern;
import no.nav.bidrag.tilgangskontroll.exception.SakIkkeFunnetException;
import no.nav.bidrag.tilgangskontroll.exception.SecurityConstraintException;
import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.response.Advice;
import no.nav.bidrag.tilgangskontroll.response.Decision;
import no.nav.bidrag.tilgangskontroll.response.XacmlResponse;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:secret.properties")
@Slf4j
public class AccessControlService {

  public static final String PEP_ID_BIDRAG = "bidrag";
  public static final String RESOURCE_TYPE_JOURNALPOST =
      "no.nav.abac.attributter.resource.bidrag.journalpost";
  private static final String READ = "read";
  private static final String ACCESS_DENIED =
      "ABAC: User does not have access to requested resource.";
  private static final String RESOURCE_BIDRAG_PARAGRAF19 =
      "no.nav.abac.attributter.resource.bidrag.paragraf19";
  private static final String ISSUER_AZURE_AD = "aad";
  private final AbacConsumer abacConsumer;
  private final AbacContext abacContext;
  private final PipConsumer pipConsumer;
  private final TokenValidationContextHolder tokenValidationContextHolder;
  private final String[] issuers;
  private String issuer = ISSUER_AZURE_AD;

  public AccessControlService(
      AbacConsumer abacConsumer,
      AbacContext abacContext,
      PipConsumer pipConsumer,
      TokenValidationContextHolder tokenValidationContextHolder,
      @Value("${no.nav.security.jwt.issuer}") String issuer) {

    this.abacConsumer = abacConsumer;
    this.abacContext = abacContext;
    this.pipConsumer = pipConsumer;
    this.tokenValidationContextHolder = tokenValidationContextHolder;
    this.issuer = issuer;
  }

  @Abac(
      bias = Decision.DENY,
      actions = @Abac.Attr(key = ACTION_ID, value = AccessControlService.READ))
  public void sjekkTilgangSak(String saksnr) throws SecurityConstraintException {
    Optional<PipIntern> metadataPip =
        !StringUtils.isBlank(saksnr)
            ? Optional.ofNullable(pipConsumer.getMetaDataPip(saksnr))
            : Optional.empty();
    if (metadataPip.isPresent()) {
      sjekkTilgangAlleRoller(metadataPip.get().getRoller(), metadataPip.get().getErParagraf19());
    } else {
      log.error("Sak ikke funnet: " + saksnr);
      throw new SakIkkeFunnetException();
    }
  }

  @Abac(
      bias = Decision.DENY,
      actions = @Abac.Attr(key = ACTION_ID, value = AccessControlService.READ))
  public void sjekkTilgangPerson(String fnr) throws SecurityConstraintException {
    sjekkTilgangAlleRoller(List.of(fnr), false);
  }

  private void sjekkTilgangAlleRoller(List<String> roller, boolean erParagraf19Sak)
      throws SecurityConstraintException {

    var request = abacContext.getRequest();

    request.failOnIndeterminate(true);
    request.environment(NavAttributter.ENVIRONMENT_FELLES_PEP_ID, PEP_ID_BIDRAG);
    request.resource(NavAttributter.RESOURCE_FELLES_DOMENE, PEP_ID_BIDRAG);
    request.resource(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_TYPE_JOURNALPOST);
    request.resource(RESOURCE_BIDRAG_PARAGRAF19, erParagraf19Sak);
    
    if (ISSUER_AZURE_AD.equals(issuers)) {
      request.environment(
          NavAttributter.SUBJECT_FELLES_AZURE_OID, getIdTokenPayloadFromContext(issuers));
    } else {
      request.environment(
          NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, getIdTokenPayloadFromContext(issuers));
    }

    for (String fnr : roller) {
      request.resource(NavAttributter.RESOURCE_FELLES_PERSON_FNR, fnr);
      evaluate(request, fnr);
    }
  }

  private void evaluate(XacmlRequest request, String id) throws SecurityConstraintException {
    XacmlResponse accessResponse;

    accessResponse = abacConsumer.evaluate(request);

    if (Decision.PERMIT != accessResponse.getDecision()) {
      throw new SecurityConstraintException(ACCESS_DENIED);
    }
  }

  private String getIdTokenPayloadFromContext(String[] issuers) throws SecurityConstraintException {

    String errorMsg =
        String.format("No idtokens found for any of the issuers provided %s", issuers);
    String idToken = "";
    for (String issuer : issuers) {
      idToken = fetchIdToken(issuer);
      if (idToken.length() > 0) {
        this.issuer = issuer;
        break;
      }
    }

    if (idToken != null) {
      log.debug("Idtoken found for issuer {}", this.issuer);

      try {
        SignedJWT signedJwt = parseIdToken(idToken);

        Base64URL[] base64URL = signedJwt.getParsedParts();

        Base64URL payload = base64URL[1];

        return payload.toString();

      } catch (ParseException pe) {
        errorMsg = String.format("Parsing of idtoken failed for issuer %s: %s", issuers, pe);
        log.error(errorMsg);

      } catch (NullPointerException npe) {
        errorMsg = String.format("Idtoken payload was null for issuer issuer %s: %s", issuers, npe);
        log.error(errorMsg);

      } catch (Exception e) {
        errorMsg =
            String.format(
                "Exception occurred when obtaining idtoken payload for issuer issuer %s: %s",
                issuers, e);
        log.error(errorMsg);
      }
    }

    throw new SecurityConstraintException(errorMsg);
  }

  private String fetchIdToken(String issuer) {
    TokenValidationContext tokenValidationContext =
        tokenValidationContextHolder.getTokenValidationContext();

    if (tokenValidationContext == null) {
      throw new IllegalStateException("Ingen TokenValidationContext found!");
    }

    Optional<JwtToken> jwtToken = tokenValidationContext.getJwtTokenAsOptional(issuer);

    if (jwtToken.isEmpty()) {
      throw new IllegalStateException("Ingen TokenContext for " + issuer);
    }

    return jwtToken.get().getTokenAsString();
  }
}
