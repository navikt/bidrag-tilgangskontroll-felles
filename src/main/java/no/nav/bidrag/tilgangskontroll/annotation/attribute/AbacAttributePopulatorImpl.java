package no.nav.bidrag.tilgangskontroll.annotation.attribute;

import static no.nav.bidrag.tilgangskontroll.annotation.attribute.AttributeAssigners.ACTION;
import static no.nav.bidrag.tilgangskontroll.annotation.attribute.AttributeAssigners.ENVIRONMENT;
import static no.nav.bidrag.tilgangskontroll.annotation.attribute.AttributeAssigners.RESOURCE;
import static no.nav.bidrag.tilgangskontroll.annotation.attribute.AttributeAssigners.SUBJECT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.nav.bidrag.tilgangskontroll.request.XacmlRequest;
import no.nav.bidrag.tilgangskontroll.exception.MissingAttributeLocatorException;

public class AbacAttributePopulatorImpl implements AbacAttributePopulator {
    private final Map<String, AbacAttributeLocator> locators;
    private final Set<String> defaultResources;
    private final Set<String> defaultSubjects;
    private final Set<String> defaultActions;
    private final Set<String> defaultEnvironments;

    public AbacAttributePopulatorImpl(List<AbacAttributeLocator> locators,
                                      Set<String> defaultResources,
                                      Set<String> defaultSubjects,
                                      Set<String> defaultActions,
                                      Set<String> defaultEnvironments) {

        this.locators = new HashMap<>(locators.size());
        for (AbacAttributeLocator locator : locators) {
            this.locators.put(locator.getAttribute(), locator);
        }
        this.defaultResources = new HashSet<>(defaultResources);
        this.defaultSubjects = new HashSet<>(defaultSubjects);
        this.defaultActions = new HashSet<>(defaultActions);
        this.defaultEnvironments = new HashSet<>(defaultEnvironments);
    }

    @Override
    public void populate(XacmlRequest request, Abac abac) {
        request.failOnIndeterminate(abac.failOnIndeterminate());
        request.bias(abac.bias());

        assign(request, abac.resources(), defaultResources, RESOURCE);
        assign(request, abac.subjects(), defaultSubjects, SUBJECT);
        assign(request, abac.actions(), defaultActions, ACTION);
        assign(request, abac.environments(), defaultEnvironments, ENVIRONMENT);
    }

    private void assign(XacmlRequest request, Abac.Attr[] attrs, Set<String> defaults, AttributeAssigner assigner) {
        for (String defaultAttribute : defaults) {
            Object val = findLocator(defaultAttribute).getValue();
            assigner.assign(request, defaultAttribute, val);
        }

        for (Abac.Attr attr : attrs) {
            Object value;
            if (!attr.value().isEmpty()) {
                value = attr.value();
            } else {
                value = findLocator(attr.key()).getValue();
            }
            assigner.assign(request, attr.key(), value);
        }
    }

    private AbacAttributeLocator findLocator(String id) {
        AbacAttributeLocator locator = locators.get(id);
        if (locator == null) {
            throw new MissingAttributeLocatorException("Failed to find AbacAttributeLocator for Attribute: " + id);
        }
        return locator;
    }
}