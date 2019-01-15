package org.bahmni.mart.form2.model;

public class Form2Control {

    private Concept concept;
    private ControlProperties properties;

    public ControlProperties getProperties() {
        return properties;
    }

    public void setProperties(ControlProperties properties) {
        this.properties = properties;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
