package no.nav.bidrag.tilgangskontroll.felles.annotation.attribute;

import lombok.AllArgsConstructor;
import no.nav.bidrag.tilgangskontroll.felles.annotation.context.AbacContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@AllArgsConstructor
public class AbacMethodInterceptor implements MethodInterceptor {

  private final AbacContext context;
  private final AbacAttributePopulator populator;

  @Override
  public Object invoke(MethodInvocation mi) throws Throwable {
    Abac abac = mi.getMethod().getAnnotation(Abac.class);

    populator.populate(context.getRequest(), abac);

    try {
      return mi.proceed();
    } finally {
      context.cleanUp();
    }
  }
}