package no.nav.bidrag.tilgangskontroll.felles.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.tilgangskontroll.felles.exception.JsonUnmarshallingException;
import org.springframework.stereotype.Component;

@Component
public class AbacResponseMapper {

  private static final Factory<Obligation> OBLIGATION_FACTORY = new Factory<Obligation>() {
    @Override
    public Obligation create(String id, List<AttributeAssignment> assignments) {
      return Obligation.builder().id(id).attributeAssignments(assignments).build();
    }
  };
  private static final Factory<Advice> ADVICE_FACTORY = new Factory<Advice>() {
    @Override
    public Advice create(String id, List<AttributeAssignment> assignments) {
      return Advice.builder().id(id).attributeAssignments(assignments).build();
    }
  };

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public XacmlResponse map(String json) {
    try {
      JsonNode node = objectMapper.readTree(json);

      Decision decision = Decision.findByValue(getDecision(node));
      List<Obligation> obligations = getObligationOrAdvice(node, "Obligations", OBLIGATION_FACTORY);
      List<Advice> advices = getObligationOrAdvice(node, "AssociatedAdvice", ADVICE_FACTORY);

      return new XacmlResponse(decision, decision, obligations, advices);
    } catch (IOException e) {
      throw new JsonUnmarshallingException("Failed to unmarshall json: \n" + json, e);
    }
  }

  private String getDecision(JsonNode node) {
    return getFieldValue(node.get("Response"), "Decision");
  }

  private <T> List<T> getObligationOrAdvice(JsonNode rootNode, String path, Factory<T> factory) {
    JsonNode node = rootNode.get("Response").get(path);
    List<T> items = new ArrayList<>();
    if (node instanceof ArrayNode) {
      for (JsonNode child : node) {
        String id = getFieldValue(child, "Id");
        List<AttributeAssignment> assignments = getAttributeAssignments(child.get("AttributeAssignment"));
        items.add(factory.create(id, assignments));
      }
    } else if (node instanceof ObjectNode) {
      String id = getFieldValue(node, "Id");
      List<AttributeAssignment> assignments = getAttributeAssignments(node.get("AttributeAssignment"));
      items.add(factory.create(id, assignments));
    }
    return items;
  }

  private List<AttributeAssignment> getAttributeAssignments(JsonNode node) {
    List<AttributeAssignment> result = new ArrayList<>();
    if (node instanceof ArrayNode) {
      for (JsonNode child : node) {
        result.add(mapAttributeAssignment(child));
      }
    } else if (node instanceof ObjectNode) {
      result.add(mapAttributeAssignment(node));
    }
    return result;
  }

  private AttributeAssignment mapAttributeAssignment(JsonNode node) {
    return AttributeAssignment.builder()
        .attributeId(getFieldValue(node, "AttributeId"))
        .value(getFieldValue(node, "Value"))
        .category(getFieldValue(node, "Category"))
        .dataType(getFieldValue(node, "DataType"))
        .build();
  }

  private String getFieldValue(JsonNode node, String field) {
    JsonNode subNode = node.get(field);
    if (subNode != null) {
      return subNode.asText();
    }
    return null;
  }

  private interface Factory<T> {

    T create(String id, List<AttributeAssignment> assignments);
  }
}