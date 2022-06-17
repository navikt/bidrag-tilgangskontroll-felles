package no.nav.bidrag.tilgangskontroll.service;

import static no.nav.bidrag.tilgangskontroll.SecurityUtils.hentSubjectIdFraAzureToken;
import static no.nav.bidrag.tilgangskontroll.SecurityUtils.henteIssuer;
import static no.nav.bidrag.tilgangskontroll.SecurityUtils.parseIdToken;
import static no.nav.bidrag.tilgangskontroll.config.StandardAttributter.ACTION_ID;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import no.nav.bidrag.tilgangskontroll.SecurityUtils;
import no.nav.bidrag.tilgangskontroll.annotation.attribute.Abac;
import no.nav.bidrag.tilgangskontroll.annotation.context.AbacContext;
import no.nav.bidrag.tilgangskontroll.config.NavAttributter;
import no.nav.bidrag.tilgangskontroll.consumer.AbacConsumer;
import no.nav.bidrag.tilgangskontroll.consumer.PipConsumer;
import no.nav.bidrag.tilgangskontroll.dto.PipIntern;
import no.nav.bidrag.tilgangskontroll.exception.SakIkkeFunnetException;
import no.nav.bidrag.tilgangskontroll.exception.SecurityConstraintException;
import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.response.Decision;
import no.nav.bidrag.tilgangskontroll.response.XacmlResponse;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.apache.commons.lang3.StringUtils;
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
  private static final String ISSUER_AZURE_AD_IDENTIFIER = "login.microsoftonline.com";
  private final AbacConsumer abacConsumer;
  private final AbacContext abacContext;
  private final PipConsumer pipConsumer;
  private final TokenValidationContextHolder tokenValidationContextHolder;

  public AccessControlService(
      AbacConsumer abacConsumer,
      AbacContext abacContext,
      PipConsumer pipConsumer,
      TokenValidationContextHolder tokenValidationContextHolder) {

    this.abacConsumer = abacConsumer;
    this.abacContext = abacContext;
    this.pipConsumer = pipConsumer;
    this.tokenValidationContextHolder = tokenValidationContextHolder;
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
      log.error("Sak ikke funnet: {}", saksnr);
      throw new SakIkkeFunnetException("Sak ble ikke funnet i PIP!");
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

    if (isTokenIssuerAzure()) {
      var idToken = henteIdToken();
      if (SecurityUtils.isSystemUser(idToken)){
        log.info("Token er Azure service-service token, hopper over tilgangskontroll");
        // Assuming bidrag apps is using zero trust pre-authorized apps
        // Change this when apps need different access control
        return;
      }
      log.info(
        "Legger til attributter for Azure token med {} InternBruker og {} fra NavIdent claim på token", NavAttributter.SUBJECT_FELLES_SUBJECT_TYPE, NavAttributter.SUBJECT_FELLES_SUBJECT_ID);
      request.accessSubject(NavAttributter.SUBJECT_FELLES_SUBJECT_TYPE, "InternBruker");
      request.accessSubject(NavAttributter.SUBJECT_FELLES_SUBJECT_ID, hentSubjectIdFraAzureToken(idToken));
    } else {
      var idToken = henteIdToken();
      log.info(
          "Legger isso-token-body inn i {}", NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY);
      request.environment(
          NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, henteTokenPayload(idToken));
    }

    for (String fnr : roller) {
      request.resource(NavAttributter.RESOURCE_FELLES_PERSON_FNR, fnr);
      evaluate(request, fnr);
    }
  }

  private boolean isTokenIssuerAzure(){
    var idToken = henteIdToken();
    var issuer = henteIssuer(idToken);

    log.info("issuer: {}", issuer);
    return issuer.contains(ISSUER_AZURE_AD_IDENTIFIER);
  }

  private void evaluate(XacmlRequest request, String id) throws SecurityConstraintException {
    XacmlResponse accessResponse;

    accessResponse = abacConsumer.evaluate(request);

    if (Decision.PERMIT != accessResponse.getDecision()) {
      throw new SecurityConstraintException(ACCESS_DENIED);
    }
  }

  private String henteTokenPayload(String idToken) {
    var errorMsg = String.format("Henting av token payload feilet!");
    try {
      SignedJWT signedJwt = parseIdToken(idToken);

      Base64URL[] base64URL = signedJwt.getParsedParts();

      Base64URL payload = base64URL[1];

      return payload.toString();

    } catch (ParseException pe) {
      errorMsg = String.format("Parsing av id-token failet!", pe);
      log.error(errorMsg);

    } catch (NullPointerException npe) {
      errorMsg = String.format("Id-token payload var null!", npe);
      log.error(errorMsg);

    } catch (Exception e) {
      errorMsg =
          String.format("Exception inntraff ved uthenting av idtoken payload", e);
      log.error(errorMsg);
    }

    throw new SecurityConstraintException(errorMsg);
  }

  private String henteIdToken() {
    TokenValidationContext tokenValidationContext =
        tokenValidationContextHolder.getTokenValidationContext();

    if (tokenValidationContext == null) {
      log.info("Ingen TokenValidationContext funnet");
      throw new IllegalStateException("Fant ingen id-token!");
    }

    Optional<JwtToken> jwtToken = tokenValidationContext.getFirstValidToken();

    if (jwtToken.isEmpty()) {
      log.info("Fant ingen id-token i header");
      throw new IllegalStateException("Fant ingen id-token!");
    }

    return jwtToken.get().getTokenAsString();
  }
}
