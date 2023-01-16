package no.nav.bidrag.tilgangskontroll.felles.annotation.context;

import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;

public class ThreadLocalAbacContext implements AbacContext {

  private final ThreadLocal<XacmlRequest> threadLocalRequest = new ThreadLocal<XacmlRequest>() {
    @Override
    protected XacmlRequest initialValue() {
      return new XacmlRequest();
    }
  };

  @Override
  public XacmlRequest getRequest() {
    return threadLocalRequest.get();
  }

  @Override
  public void cleanUp() {
    threadLocalRequest.remove();
  }
}