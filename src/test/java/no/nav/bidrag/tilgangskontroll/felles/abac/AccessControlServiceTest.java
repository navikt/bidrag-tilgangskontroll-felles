package no.nav.bidrag.tilgangskontroll.felles.abac;

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
import no.nav.bidrag.tilgangskontroll.felles.Testapp;
import no.nav.bidrag.tilgangskontroll.felles.annotation.context.AbacContext;
import no.nav.bidrag.tilgangskontroll.felles.config.NavAttributter;
import no.nav.bidrag.tilgangskontroll.felles.consumer.AbacConsumer;
import no.nav.bidrag.tilgangskontroll.felles.consumer.PipConsumer;
import no.nav.bidrag.tilgangskontroll.felles.dto.PipIntern;
import no.nav.bidrag.tilgangskontroll.felles.exception.SakIkkeFunnetException;
import no.nav.bidrag.tilgangskontroll.felles.exception.SecurityConstraintException;
import no.nav.bidrag.tilgangskontroll.felles.SecurityUtils;
import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.felles.response.Advice;
import no.nav.bidrag.tilgangskontroll.felles.response.AttributeAssignment;
import no.nav.bidrag.tilgangskontroll.felles.response.Decision;
import no.nav.bidrag.tilgangskontroll.felles.response.XacmlResponse;
import no.nav.bidrag.tilgangskontroll.felles.service.AccessControlService;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DisplayName("AccessControlServiceTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Testapp.class)
@EnableMockOAuth2Server
class AccessControlServiceTest {

  private final String SAKSNR = "0000001";
  private final String FNR_TEST_PERSON = "01011112345";

  @Autowired
  private MockOAuth2Server mockOAuth2Server;

  @Captor
  private ArgumentCaptor<XacmlRequest> pdpRequestCaptor;

  @Mock
  private AbacContext abacContext;

  @Mock
  private AbacConsumer abacConsumer;

  @Mock
  private PipConsumer pipConsumer;

  @Mock
  private SpringTokenValidationContextHolder springTokenValidationContextHolder;

  @MockBean
  private TokenValidationContextHolder tokenValidationContextHolder;

  private AccessControlService accessControlService;

  private String token;

  @BeforeEach
  void generateTestToken() {
    if (token == null) {
      this.token = mockOAuth2Server.issueToken().serialize();
    }
  }

  @BeforeEach
  void initAccessControlService() {
    accessControlService = new AccessControlService(abacConsumer, abacContext, pipConsumer, springTokenValidationContextHolder);
    this.token = mockOAuth2Server.issueToken().serialize();
  }

  @Test
  @DisplayName("Skal kaste SakIkkeFunnetException dersom sak ikke eksisterer")
  void skalKasteSakIkkeFunnetExceptionDersomSakIkkeEksisterer() {
    // Given, when, then
    Assertions.assertThrows(SakIkkeFunnetException.class, () -> accessControlService.sjekkTilgangSak(SAKSNR), "SakIkkeFunnetException is expected");
  }

  @Test
  @DisplayName("Skal kaste SecurityConstraintException dersom ABAC svarer med deny for sak")
  void skalKasteSecurityConstraintExceptionDersomABACSvarerMedDenyForSak() {
    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.DENY));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());
    when(pipConsumer.getMetaDataPip(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When, then
    Assertions.assertThrows(SecurityConstraintException.class, () -> accessControlService.sjekkTilgangSak(SAKSNR), "SecurityConstraintException is expected");
  }

  @Test
  @DisplayName("Skal kaste SecurityConstraintException dersom ABAC svarer med DENY for person")
  void skalKasteSecurityConstraintExceptionDersomAbacSvarerMedDenyForPerson() {
    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.DENY));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());

    // When, then
    assertThrows(SecurityConstraintException.class, () -> accessControlService.sjekkTilgangPerson(FNR_TEST_PERSON),
        "SecurityConstraintException is expected");
  }

  @Test
  @DisplayName("Skal ikke kaste exception dersom ABAC svarer med PERMIT for person")
  void skaIkkeKasteExceptionVedPermitForSak() {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());
    when(pipConsumer.getMetaDataPip(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When, then
    assertDoesNotThrow(() -> accessControlService.sjekkTilgangSak(SAKSNR), "Expects no exceptions for PERMIT decision");
  }

  @Test
  @DisplayName("Skal ikke kaste exception dersom ABAC svarer med PERMIT for person")
  void skalIkkeKasteExceptionDersomAbacSvarerMedPermitForPerson() {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());

    // When, then
    assertDoesNotThrow(() -> accessControlService.sjekkTilgangPerson(FNR_TEST_PERSON), "Expects no exceptions for PERMIT decision");
  }

  @Test
  @DisplayName("Access sak, PDP request is formatted correctly")
  void assertCorrectFormatPdpRequestSak() throws ParseException {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());
    when(pipConsumer.getMetaDataPip(SAKSNR)).thenReturn(mockedBidragSakPipIntern());

    // When
    accessControlService.sjekkTilgangSak(SAKSNR);

    // Then
    verify(abacConsumer).evaluate(pdpRequestCaptor.capture());

    XacmlRequest pdpRequest = pdpRequestCaptor.getValue();

    assertPdpRequestFormat(pdpRequest);
  }

  @Test
  @DisplayName("Access person, PDP request formatted correctly ")
  void assertCorrectFormatPdpRequestPerson() throws ParseException {

    // Given
    when(abacContext.getRequest()).thenReturn(createXacmlRequest());
    when(abacConsumer.evaluate(any(XacmlRequest.class))).thenReturn(createXacmlResponse(Decision.PERMIT));
    when(springTokenValidationContextHolder.getTokenValidationContext()).thenReturn(createTokenValidator());

    // When
    accessControlService.sjekkTilgangPerson(FNR_TEST_PERSON);

    // Then
    verify(abacConsumer).evaluate(pdpRequestCaptor.capture());

    XacmlRequest pdpRequest = pdpRequestCaptor.getValue();

    assertPdpRequestFormat(pdpRequest);
  }

  private void assertPdpRequestFormat(XacmlRequest pdpRequest) throws ParseException {
    assertThat(pdpRequest.getEnvironments()).as("The PDP-request is expected to hold two environment attributes.").hasSize(2);

    assertThat(pdpRequest.getResources()).as("The PDP-request is expected to hold four resource attributes").hasSize(4);

    assertThat(pdpRequest.getEnvironment(NavAttributter.ENVIRONMENT_FELLES_PEP_ID).toString()).isEqualToIgnoringCase(
        AccessControlService.PEP_ID_BIDRAG);

    Base64URL idTokenPayload = SecurityUtils.parseIdToken(token).getParsedParts()[1];
    assertThat(pdpRequest.getEnvironment(NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY).toString()).isEqualToIgnoringCase(idTokenPayload.toString());

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_DOMENE).toString()).isEqualToIgnoringCase(AccessControlService.PEP_ID_BIDRAG);

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_PERSON_FNR).toString()).isEqualToIgnoringCase(FNR_TEST_PERSON);

    assertThat(pdpRequest.getResource(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE).toString()).isEqualToIgnoringCase(
        AccessControlService.RESOURCE_TYPE_JOURNALPOST);
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
        AttributeAssignment.builder().attributeId("no.nav.abac.attributter.adviceorobligation.cause").value("cause-0001-manglerrolle")
            .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment").dataType("http://www.w3.org/2001/XMLSchema#string)").build());

    attributeAssignments.add(
        AttributeAssignment.builder().attributeId("no.nav.abac.attributter.adviceorobligation.deny_policy").value("internbruker_basis")
            .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment").dataType("http://www.w3.org/2001/XMLSchema#string)").build());

    attributeAssignments.add(AttributeAssignment.builder().attributeId("no.nav.abac.attributter.adviceorobligation.deny_rule").value("basisrolle_NOK")
        .category("urn:oasis:names:tc:xacml:3.0:attribute-category:environment").dataType("http://www.w3.org/2001/XMLSchema#string)").build());

    advices.add(Advice.builder().id("no.nav.abac.advices.reason.deny_reason").attributeAssignments(attributeAssignments).build());

    return advices;
  }

  private TokenValidationContext createTokenValidator(String token2) {
    var jwtToken = new JwtToken(token2);
    var tokenMap = new HashMap<String, JwtToken>();
    tokenMap.put("aad", jwtToken);

    return new TokenValidationContext(tokenMap);
  }
  private TokenValidationContext createTokenValidator() {
    var jwtToken = new JwtToken(token);
    var tokenMap = new HashMap<String, JwtToken>();
    tokenMap.put("aad", jwtToken);

    return new TokenValidationContext(tokenMap);
  }

  private PipIntern mockedBidragSakPipIntern() {
    return PipIntern.builder().erParagraf19(false).roller(new ArrayList<>(Collections.singletonList(FNR_TEST_PERSON))).saksnummer("123456789")
        .build();
  }
}
