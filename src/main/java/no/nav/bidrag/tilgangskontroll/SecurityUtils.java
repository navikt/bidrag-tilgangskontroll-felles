package no.nav.bidrag.tilgangskontroll;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);
  private static final String ISSUER_AZURE_AD_IDENTIFIER = "login.microsoftonline.com";

  /**
   * Returns a Base64 encoded string comprised of username:password
   *
   * @param username to encode
   * @param password to encode
   * @return Base64 encoded username:password string
   */
  public static String base64EncodeCredentials(String username, String password) {

    String credentials = username + ":" + password;

    byte[] encodedCredentials = Base64.getEncoder().encode(credentials.getBytes());

    return new String(encodedCredentials, StandardCharsets.UTF_8);
  }

  public static SignedJWT parseIdToken(String idToken) throws ParseException {
    return (SignedJWT) JWTParser.parse(idToken);
  }

  /**
   * @param idToken to parse
   * @return subject from token
   */
  public static String henteSubject(String idToken) {
    LOGGER.info("Skal finne subject fra id-token");

    try {
      return henteSubject(parseIdToken(idToken));
    } catch (Exception e) {
      LOGGER.error("Klarte ikke parse " + idToken, e);

      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      }

      throw new IllegalArgumentException("Klarte ikke å parse " + idToken, e);
    }
  }

  /**
   * @param idToken to parse
   * @return pid from token
   */
  public static String hentePid(String idToken) {
    LOGGER.info("Skal finne pid fra id-token");

    try {
      return hentePid(parseIdToken(idToken));
    } catch (Exception e) {
      LOGGER.error("Klarte ikke parse " + idToken, e);

      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      }

      throw new IllegalArgumentException("Klarte ikke å parse " + idToken, e);
    }
  }

  private static String henteSubject(SignedJWT signedJWT) {
    try {
      if (isTokenIssuedByAzure(signedJWT)){
        return hentSubjectIdFraAzureToken(signedJWT);
      }
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets subject", e);
    }
  }

  public static boolean isTokenIssuedByAzure(SignedJWT signedJWT){
    try {
      var issuer = signedJWT.getJWTClaimsSet().getIssuer();
      return isTokenIssuedByAzure(issuer);
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets subject", e);
    }
  }

  public static boolean isTokenIssuedByAzure(String issuer){
    return issuer != null && issuer.contains(ISSUER_AZURE_AD_IDENTIFIER);
  }

  private static String hentePid(SignedJWT signedJWT) {
    try {
      return signedJWT.getJWTClaimsSet().getStringClaim("pid");
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets pid", e);
    }
  }

  public static String henteIssuer(String idToken) {
    try {
      var t = parseIdToken(idToken);
      return parseIdToken(idToken).getJWTClaimsSet().getIssuer();
    }catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets issuer", e);
    }
  }

  public static boolean isSystemUser(String idToken) {
    try {
      var claims = parseIdToken(idToken).getJWTClaimsSet();
      var systemRessurs = "Systemressurs".equals(claims.getStringClaim("identType"));
      var roles = claims.getStringListClaim("roles");
      var azureApp = roles != null && roles.contains("access_as_application");
      return systemRessurs || azureApp;
    }catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets issuer", e);
    }
  }

  public static String hentSubjectIdFraAzureToken(SignedJWT signedJWT) {
    try {
      var claims = signedJWT.getJWTClaimsSet();
      var navIdent = claims.getStringClaim("NAVident");
      var application = claims.getStringClaim("azp_name");
      return navIdent != null ? navIdent : getApplicationNameFromAzp(application);
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets issuer", e);
    }
  }

  private static String getApplicationNameFromAzp(String azpName){
    if (azpName == null){
      return null;
    }
    var azpNameSplit = azpName.split(":");
    return azpNameSplit[azpNameSplit.length - 1];
  }

  public static String hentSubjectIdFraAzureToken(String idToken) {
    try {
      return hentSubjectIdFraAzureToken(parseIdToken(idToken));
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om tokenets issuer", e);
    }
  }
}
