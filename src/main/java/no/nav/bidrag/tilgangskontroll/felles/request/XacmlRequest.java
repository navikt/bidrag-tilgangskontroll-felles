package no.nav.bidrag.tilgangskontroll.felles.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.nav.bidrag.tilgangskontroll.felles.response.Decision;

@Getter
@ToString
@NoArgsConstructor
public class XacmlRequest {

  private List<XacmlAttribute> resources = new ArrayList<>();
  private List<XacmlAttribute> accessSubjects = new ArrayList<>();
  private List<XacmlAttribute> actions = new ArrayList<>();
  private List<XacmlAttribute> environments = new ArrayList<>();
  private boolean failOnIndeterminate = false;
  private Decision bias;

  /**
   * Copy constructor for XACML-request. Performs shallowCopy of underlying collections
   *
   * @param xacmlRequest
   */
  public XacmlRequest(XacmlRequest xacmlRequest) {
    resources = new ArrayList<>(xacmlRequest.getResources());
    accessSubjects = new ArrayList<>(xacmlRequest.getAccessSubjects());
    actions = new ArrayList<>(xacmlRequest.getActions());
    environments = new ArrayList<>(xacmlRequest.getEnvironments());
    failOnIndeterminate = xacmlRequest.failOnIndeterminate;
    bias = xacmlRequest.getBias();
  }

  public XacmlRequest resource(String id, Object value) {
    resources.add(new XacmlAttribute(id, value));
    return this;
  }

  public Object getResource(String id) {
    return findAttribute(id, resources);
  }

  public XacmlRequest accessSubject(String id, Object value) {
    accessSubjects.add(new XacmlAttribute(id, value));
    return this;
  }

  public XacmlRequest action(String id, Object value) {
    actions.add(new XacmlAttribute(id, value));
    return this;
  }

  public XacmlRequest environment(String id, Object value) {
    environments.add(new XacmlAttribute(id, value));
    return this;
  }

  public Object getEnvironment(String id) {
    return findAttribute(id, environments);
  }

  public XacmlRequest bias(Decision bias) {
    this.bias = bias;
    return this;
  }

  public XacmlRequest failOnIndeterminate(boolean val) {
    this.failOnIndeterminate = val;
    return this;
  }

  private Object findAttribute(String id, List<XacmlAttribute> attributes) {
    for (XacmlAttribute attribute : attributes) {
      if (attribute.getAttributeId().equals(id)) {
        return attribute.getValue();
      }
    }
    return null;
  }
}