package no.nav.bidrag.tilgangskontroll.felles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Testapp {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Testapp.class);
    app.setAdditionalProfiles("local");
    app.run(args);
  }
}
