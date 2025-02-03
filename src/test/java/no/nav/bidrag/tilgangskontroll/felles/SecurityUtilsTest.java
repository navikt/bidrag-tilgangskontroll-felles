package no.nav.bidrag.tilgangskontroll.felles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

  // Generated using http://jwtbuilder.jamiekurtz.com/
  private static String issoUser = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2lzc28tcS5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsImlhdCI6MTY1NTg3NzQyNCwiZXhwIjoxNjg3NDEzNDI0LCJhdWQiOiJiaWRyYWctdWktZmVhdHVyZS1xMSIsInN1YiI6Ilo5OTQ5NzciLCJ0b2tlbk5hbWUiOiJpZF90b2tlbiIsImF6cCI6ImJpZHJhZy11aS1mZWF0dXJlLXExIn0.NYxxExStmzxqvjf-uKn7EnT9rOzluRxipclj0IH_0XQ";
  private static String stsToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3NlY3VyaXR5LXRva2VuLXNlcnZpY2UubmFpcy5wcmVwcm9kLmxvY2FsIiwiaWF0IjoxNjU1ODc3NDI0LCJleHAiOjE2ODc0MTM0MjQsImF1ZCI6InNydmJpc3lzIiwic3ViIjoic3J2YmlzeXMiLCJpZGVudFR5cGUiOiJTeXN0ZW1yZXNzdXJzIiwiYXpwIjoic3J2YmlzeXMifQ.ivpkYHclkl9z3fOfCSIMKKOsRSOGzr-y9AqerJEy9BA";
  private static String azureSystemToken = "";
  private static String azureUserToken = "";

  @Test
  void skalHentePidFraToken() {

    // given
    var testident = "03827297045";
    var testIdtoken = "";

    // when
    var pid = SecurityUtils.hentePid(testIdtoken);

    // then
    assertThat(pid).isEqualTo(testident);
  }

  @Test
  void skalHenteSubjectFraAzureSystemToken() {

    var subject = SecurityUtils.henteSubject(azureSystemToken);

    // then
    assertThat(subject).isEqualTo("bidrag-dokument-feature");
  }

  @Test
  void skalHenteSubjectFraAzureToken() {

    var subject = SecurityUtils.henteSubject(azureUserToken);

    // then
    assertThat(subject).isEqualTo("Z994977");
  }

  @Test
  void skalHenteSubjectFraIssoToken() {

    var subject = SecurityUtils.henteSubject(issoUser);

    // then
    assertThat(subject).isEqualTo("Z994977");
  }

  @Test
  void shouldValidateSystemToken() {

    // when
    var resultAzure = SecurityUtils.isSystemUser(azureSystemToken);
    var resultSTS = SecurityUtils.isSystemUser(stsToken);
    var resultAzureUser = SecurityUtils.isSystemUser(azureUserToken);
    var resultIsso = SecurityUtils.isSystemUser(issoUser);

    // then
    assertThat(resultAzure).isTrue();
    assertThat(resultSTS).isTrue();
    assertThat(resultAzureUser).isFalse();
    assertThat(resultIsso).isFalse();
  }

  @Test
  void shouldGetNavUserIdent() {
    assertThat(SecurityUtils.hentSubjectIdFraAzureToken(azureUserToken)).isEqualTo("Z994977");
  }
}
