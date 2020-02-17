package no.nav.bidrag.tilgangskontroll.abac;

import static no.nav.bidrag.tilgangskontroll.SecurityUtils.parseIdToken;
import static no.nav.bidrag.tilgangskontroll.abac.AccessControlService.STANDARD_ISSUER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.util.Base64URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import no.nav.abac.xacml.NavAttributter;
import no.nav.bidrag.tilgangskontroll.consumer.AbacConsumer;
import no.nav.bidrag.tilgangskontroll.consumer.BidragSakConsumer;
import no.nav.bidrag.tilgangskontroll.dto.BidragSakPipIntern;
import no.nav.bidrag.tilgangskontroll.abac.annotation.context.AbacContext;
import no.nav.bidrag.tilgangskontroll.abac.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.abac.response.Advice;
import no.nav.bidrag.tilgangskontroll.abac.response.AttributeAssignment;
import no.nav.bidrag.tilgangskontroll.abac.response.Decision;
import no.nav.bidrag.tilgangskontroll.abac.response.XacmlResponse;
import no.nav.bidrag.tilgangskontroll.exception.SakIkkeFunnetException;
import no.nav.bidrag.tilgangskontroll.exception.SecurityConstraintException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder;
import no.nav.security.token.support.test.jersey.TestTokenGeneratorResource;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DisplayName("AccessControlServiceTest")
@Import(TokenGeneratorConfiguration.class)
class AccessControlServiceTest {

  private String testIdToken;
  private final String SAKSNR = "0000001";
  private final String FNR_TEST_PERSON = "01011112345";

  @Captor
  private ArgumentCaptor<XacmlRequest> pdpRequestCaptor;

  @Mock
  private AbacContext abacContext;

  @Mock
  private AbacConsumer abacConsumer;

  @Mock
  private BidragSakConsumer bidragSakConsumer;

  @Mock
  private SpringTokenValidationContextHolder springTokenValidationContextHolder;

  private AccessControlService accessControlService;

  @BeforeEach
  void generateTestToken() {
    if (testIdToken == null) {
      TestTokenGeneratorResource testTokenGeneratorResource = new TestTokenGeneratorResource();
      this.testIdToken = testTokenGeneratorResource.issueToken("localhost-idtoken");
    }
  }

  @BeforeEach
  void initAccessControlService() {
    accessControlService = new AccessControlService(
        abacConsumer, abacContext, bidragSakConsumer, springTokenValidationContextHolder, new String[]{STANDARD_ISSUER});
  }

  @Test
  @DisplayName("Throw SakIkkeFunnetException")
  void sakMissing() {
    // Given, when, then
    assertThrows(
        SakIkkeFunnetException.class,
        () -> accessControlService.sjekkTilgangSak(SAKSNR),
        "SakIkkeFunnetException is expected");
  }

  @Test
  @DisplayName("Throw SecurityConstraintException if PDP responds with DENY decision")
  void accessDenied() {
    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class)))
        .thenReturn(createXacmlResponse(Decision.DENY));
    when(springTokenValidationContextHolder.getTokenValidationContext())
        .thenReturn(createTokenValidator());
    when(bidragSakConsumer.getMetaDataBidragsak(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When, then
    assertThrows(
        SecurityConstraintException.class,
        () -> accessControlService.sjekkTilgangSak(SAKSNR),
        "SecurityConstraintException is expected");
  }

  @Test
  @DisplayName("PDP responds with decision PERMIT")
  void accessGranted() {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class)))
        .thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext())
        .thenReturn(createTokenValidator());
    when(bidragSakConsumer.getMetaDataBidragsak(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When, then
    assertDoesNotThrow(
        () -> accessControlService.sjekkTilgangSak(SAKSNR),
        "Expects no exceptions for PERMIT decision");
  }

  @Test
  @DisplayName("PDP request is formatted correctly")
  void assertCorrectFormatPdpRequest() throws ParseException {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class)))
        .thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext())
        .thenReturn(createTokenValidator());
    when(bidragSakConsumer.getMetaDataBidragsak(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When
    accessControlService.sjekkTilgangSak(SAKSNR);

    // Then
    verify(abacConsumer).evaluate(pdpRequestCaptor.capture());

    XacmlRequest pdpRequest = pdpRequestCaptor.getValue();

    assertThat(pdpRequest.getEnvironments()).as("The PDP-request is expected to hold two environment attributes.").hasSize(2);

    assertThat(pdpRequest.getResources()).as("The PDP-request is expected to hold four resource attributes").hasSize(4);

    assertThat(pdpRequest.getEnvironment(NavAttributter.ENVIRONMENT_FELLES_PEP_ID).toString())
        .isEqualToIgnoringCase(AccessControlService.PEP_ID_BIDRAG);

    Base64URL idTokenPayload = parseIdToken(testIdToken).getParsedParts()[1];

    assertThat(pdpRequest.getEnvironment(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY).toString())
        .isEqualToIgnoringCase(idTokenPayload.toString());

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_DOMENE).toString())
        .isEqualToIgnoringCase(AccessControlService.PEP_ID_BIDRAG);

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_PERSON_FNR).toString())
        .isEqualToIgnoringCase(FNR_TEST_PERSON);

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE).toString())
        .isEqualToIgnoringCase(AccessControlService.RESOURCE_TYPE_JOURNALPOST);
  }

  private XacmlRequest createXacmlRequest() {
    return new XacmlRequest();
  }

  private XacmlResponse createXacmlResponse(Decision decision) {
    List<Advice> advices = decision.equals(Decision.DENY) ? createAdvices() : new ArrayList<>();
    return new XacmlResponse(decision, decision, null, advices);
  }

  private List<Advice> createAdvices() {

    List<Advice> advices = new ArrayList<>();
    List<AttributeAssignment> attributeAssignments = new ArrayList<>();

    attributeAssignments.add(
        AttributeAssignment.builder()
            .attributeId("no.nav.abac.attributter.adviceorobligation.cause")
            .value("cause-0001-manglerrolle")
            .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
            .dataType("http://www.w3.org/2001/XMLSchema#string)")
            .build());

    attributeAssignments.add(
        AttributeAssignment.builder()
            .attributeId("no.nav.abac.attributter.adviceorobligation.deny_policy")
            .value("internbruker_basis")
            .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
            .dataType("http://www.w3.org/2001/XMLSchema#string)")
            .build());

    attributeAssignments.add(
        AttributeAssignment.builder()
            .attributeId("no.nav.abac.attributter.adviceorobligation.deny_rule")
            .value("basisrolle_NOK")
            .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment")
            .dataType("http://www.w3.org/2001/XMLSchema#string)")
            .build());

    advices.add(
        Advice.builder()
            .id("no.nav.abac.advices.reason.deny_reason")
            .attributeAssignments(attributeAssignments)
            .build());

    return advices;
  }

  private TokenValidationContext createTokenValidator() {
    var jwtToken = new JwtToken(testIdToken);
    var tokenMap = new HashMap<String, JwtToken>();
    tokenMap.put(STANDARD_ISSUER, jwtToken);

    return new TokenValidationContext(tokenMap);
  }

  private BidragSakPipIntern mockedBidragSakPipIntern() {
    BidragSakPipIntern bidragSakPipIntern = new BidragSakPipIntern();
    bidragSakPipIntern.setRoller(new ArrayList<>(Collections.singletonList(FNR_TEST_PERSON)));
    return bidragSakPipIntern;
  }
}
