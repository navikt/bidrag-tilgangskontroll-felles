package no.nav.bidrag.tilgangskontroll.felles.annotation.attribute;

import no.nav.bidrag.tilgangskontroll.felles.request.XacmlRequest;

class AttributeAssigners {

  static final AttributeAssigner RESOURCE = new AttributeAssigner() {
    @Override
    public void assign(XacmlRequest request, String id, Object value) {
      if (value != null) {
        request.resource(id, value);
      }
    }
  };

  static final AttributeAssigner ACTION = new AttributeAssigner() {
    @Override
    public void assign(XacmlRequest request, String id, Object value) {
      if (value != null) {
        request.action(id, value);
      }
    }
  };

  static final AttributeAssigner SUBJECT = new AttributeAssigner() {
    @Override
    public void assign(XacmlRequest request, String id, Object value) {
      if (value != null) {
        request.accessSubject(id, value);
      }
    }
  };

  static final AttributeAssigner ENVIRONMENT = new AttributeAssigner() {
    @Override
    public void assign(XacmlRequest request, String id, Object value) {
      if (value != null) {
        request.environment(id, value);
      }
    }
  };
}