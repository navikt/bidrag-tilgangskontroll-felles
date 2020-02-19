package no.nav.bidrag.tilgangskontroll.annotation.attribute;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import no.nav.bidrag.tilgangskontroll.response.Decision;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Abac {

  Attr[] resources() default {};

  Attr[] subjects() default {};

  Attr[] actions() default {};

  Attr[] environments() default {};

  boolean failOnIndeterminate() default false;

  Decision bias() default Decision.DENY;

  @interface Attr {

    String key();

    String value() default "";
  }
}