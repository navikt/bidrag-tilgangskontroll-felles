package no.nav.bidrag.tilgangskontroll;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

  @Test
  void skalHentePidFraToken() {

    // given
    var testident = "03827297045";
    var testIdtoken = "eyJraWQiOiJ2UHBaZW9HOGRkTHpmdHMxLWxnc3VnOHNyYVd3bW04dHhJaGJ3Y1h3R01JIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJkTEJSelptWE1qNENXSkU1RWlHZHJLNDctM0VnSlFOZER0SEgwbkJ5d0RJPSIsImlzcyI6Imh0dHBzOlwvXC9vaWRjLXZlcjIuZGlmaS5ub1wvaWRwb3J0ZW4tb2lkYy1wcm92aWRlclwvIiwiY2xpZW50X2FtciI6ImNsaWVudF9zZWNyZXRfcG9zdCIsInBpZCI6IjAzODI3Mjk3MDQ1IiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImNsaWVudF9pZCI6ImQ4Mjk3MjQwLTIzYzgtNDBjOC1iMWM4LWEyOTNhZjczNjk3MiIsImF1ZCI6Imh0dHBzOlwvXC9uYXYubm8iLCJhY3IiOiJMZXZlbDQiLCJzY29wZSI6Im9wZW5pZCIsImV4cCI6MTY1MTQ4ODk2MSwiaWF0IjoxNjUxNDg1MzYxLCJjbGllbnRfb3Jnbm8iOiI4ODk2NDA3ODIiLCJqdGkiOiJDbC1KaTJRUjMwenV6Y2ZSdGJiLV9WRUlqbHNlc2VPekR0VzNhQzZ3eVNBIiwiY29uc3VtZXIiOnsiYXV0aG9yaXR5IjoiaXNvNjUyMy1hY3RvcmlkLXVwaXMiLCJJRCI6IjAxOTI6ODg5NjQwNzgyIn19.M-ra4lqxARFNItFolFxKdbw1-IYZzlUzqmWrq89Z09wnNlxycHc_03wJnIrYCsSQgwk-rUV20k-boQDfauDJM8Cq1DLmH-GMHx3LH0bAwi8KZI3EN57GsCePtMF-WSDKJ2o5Ny_b5_z6SANeZ6LbKVsIP_2Z6pQKni5_CbWlNzFEZ35QPQnF_9DOO_549GXIPLK6Bk1mfiG_95nunfD1JPQB_WZ-zWNd3VXEBnmSXnJqMIj0VM7GHykSRD5s4mj8NjDWVLmu82ag2c70mt0ebEopUvBRjbWZE2CMBKuJIxmzjFVaXwmXDOp-MXz7Nsw-LJ1F0qUvFhfoFykpAPxTzQ";

    // when
    var pid = SecurityUtils.hentePid(testIdtoken);

    // then
    assertThat(pid).isEqualTo(testident);
  }
}