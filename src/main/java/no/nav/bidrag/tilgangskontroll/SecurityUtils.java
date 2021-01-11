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
   * @return saksbehandler from token
   */
  public static String hentSaksbehandler(String idToken) {
    LOGGER.info("Skal finne saksbehandler fra id-token");

    try {
      return hentSaksbehandler(SecurityUtils.parseIdToken(idToken));
    } catch (Exception e) {
      LOGGER.error("Klarte ikke parse " + idToken, e);

      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      }

      throw new IllegalArgumentException("Klarte ikke å parse " + idToken, e);
    }
  }

  private static String hentSaksbehandler(SignedJWT signedJWT) {
    try {
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente saksbehandler", e);
    }
  }

  public static String henteIssuer(String idToken) {
    try {
      return parseIdToken(idToken).getJWTClaimsSet().getIssuer();
    }catch (ParseException e) {
      throw new IllegalStateException("Kunne ikke hente informasjon om issuer", e);
    }
  }
}
