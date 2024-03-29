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
  private static String azureSystemToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vdGVzdC92Mi4wIiwiaWF0IjoxNjU1ODc3MDQwLCJleHAiOjE2ODc0MTMwNDAsImF1ZCI6IjY3NjY2NDUtNTNkNS00OGY5LWJlOTctOTljN2ZjNzRmMDlhIiwic3ViIjoiNTU1NTU1LTUzZDUtNDhmOS1iZTk3LTk5YzdmYzc0ZjA5YSIsImF6cF9uYW1lIjoiZGV2LWZzczpiaWRyYWc6YmlkcmFnLWRva3VtZW50LWZlYXR1cmUiLCJyb2xlcyI6WyJhY2Nlc3NfYXNfYXBwbGljYXRpb24iLCJzb21ldGhpbmcgZWxzZSJdfQ.XvdyJCtIt-ME4t956z76xOf2hrkM7WOvTRWjI6QcYiA";
  private static String azureUserToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vdGVzdC92Mi4wIiwiaWF0IjoxNjU1ODc3MDQwLCJleHAiOjE2ODc0MTMwNDAsImF1ZCI6IjY3NjY2NDUtNTNkNS00OGY5LWJlOTctOTljN2ZjNzRmMDlhIiwic3ViIjoiNTU1NTU1LTUzZDUtNDhmOS1iZTk3LTk5YzdmYzc0ZjA5YSIsImF6cF9uYW1lIjoiZGV2LWZzczpiaWRyYWc6YmlkcmFnLXVpLWZlYXR1cmUiLCJSb2xlIjoiYWNjZXNzX2FzX2FwcGxpY2F0aW9uIiwiTkFWaWRlbnQiOiJaOTk0OTc3In0.7XhNn27iaKY-z4voUp-ZfR__5u3Rv5rJCgTpSNVW1nY";

  @Test
  void skalHentePidFraToken() {

    // given
    var testident = "03827297045";
    var testIdtoken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL29pZGMtdmVyMi5kaWZpLm5vL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXIvIiwiaWF0IjoxNjU1ODc3NDI0LCJleHAiOjE2ODc0MTM0MjQsImF1ZCI6ImJpZHJhZy11aS1mZWF0dXJlLXExIiwic3ViIjoiZExCUnpabVhNajRDV0pFNUVpR2RySzQ3IiwidG9rZW5fdHlwZSI6IkJlYXJlciIsInBpZCI6IjAzODI3Mjk3MDQ1In0.yHZ44FTM6ugU7CWqMIW1tt-QtKQc_LMyQ4eb7dhOeDI";

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
