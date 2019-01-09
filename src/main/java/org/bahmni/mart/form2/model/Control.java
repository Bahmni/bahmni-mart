package org.bahmni.mart.form2.model;

import java.util.List;

public class Control {
    private ControlProperties properties;
    private Concept concept;
    private List<Control> controls;

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }

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
