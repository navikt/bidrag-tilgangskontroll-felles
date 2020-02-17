package no.nav.bidrag.tilgangskontroll.abac.config;

import java.lang.reflect.Method;
import no.nav.bidrag.tilgangskontroll.abac.annotation.attribute.Abac;
import no.nav.bidrag.tilgangskontroll.abac.annotation.attribute.AbacMethodInterceptor;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

public class AbacAdvisor extends AbstractPointcutAdvisor {

  private static final long serialVersionUID = 1612646152135125125L;

  private static final StaticMethodMatcherPointcut POINTCUT =
      new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> aClass) {
          return method.isAnnotationPresent(Abac.class);
        }
      };

  private final AbacMethodInterceptor interceptor;

  public AbacAdvisor(AbacMethodInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public Pointcut getPointcut() {
    return POINTCUT;
  }

  @Override
  public Advice getAdvice() {
    return interceptor;
  }
}
