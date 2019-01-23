package org.bahmni.mart.form2;

import org.bahmni.mart.form2.model.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.ControlLabel;
import org.bahmni.mart.form2.model.ControlProperties;

import java.util.List;

public class ControlBuilder {

    private Control control = new Control();

    public ControlBuilder(){
        control.setProperties(new ControlProperties());
    }

    public ControlBuilder withLabel(String label){
        ControlLabel controlLabel = new ControlLabel();
        controlLabel.setValue(label);
        control.setLabel(controlLabel);
        return this;
    }

    public ControlBuilder withPropertyAddMore(boolean addMore){
        control.getProperties().setAddMore(addMore);
        return this;
    }

    public Control withPropertyMultiSelect(boolean isMultiSelect){
        control.getProperties().setAddMore(isMultiSelect);
        return control;
    }

    public ControlBuilder withConcept(String name, String uuid){
        Concept concept = new Concept();
        concept.setName(name);
        concept.setUuid(uuid);
        control.setConcept(concept);
        return this;
    }

    public ControlBuilder withControls(List<Control> controls){
        control.setControls(controls);
        return this;
    }

    public Control build() {
        return control;
    }
}
