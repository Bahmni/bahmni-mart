package org.bahmni.mart.form2;

import org.bahmni.mart.form2.model.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.ControlLabel;
import org.bahmni.mart.form2.model.ControlProperties;

import java.util.List;

public class ControlBuilder {

    private Control control = new Control();

    public ControlBuilder() {
        control.setProperties(new ControlProperties());
    }

    public ControlBuilder withLabel(String label, String translationKey) {
        ControlLabel controlLabel = new ControlLabel();
        controlLabel.setValue(label);
        controlLabel.setTranslationKey(translationKey);
        control.setLabel(controlLabel);
        return this;
    }

    ControlBuilder withPropertyAddMore(boolean addMore) {
        control.getProperties().setAddMore(addMore);
        return this;
    }

    ControlBuilder withPropertyMultiSelect(boolean isMultiSelect) {
        control.getProperties().setMultiSelect(isMultiSelect);
        return this;
    }

    ControlBuilder withConcept(String name, String uuid) {
        Concept concept = new Concept();
        concept.setName(name);
        concept.setUuid(uuid);
        control.setConcept(concept);
        return this;
    }

    ControlBuilder withControls(List<Control> controls) {
        control.setControls(controls);
        return this;
    }

    public Control build() {
        return control;
    }

    ControlBuilder withType(String section) {
        control.setType(section);
        return this;
    }
}
