package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;

public class BahmniFormBuilder {

    private BahmniForm bahmniForm = new BahmniForm();

    public BahmniFormBuilder withChild(BahmniForm form) {
        bahmniForm.addChild(form);
        return this;
    }

    public BahmniFormBuilder withName(String name) {
        bahmniForm.setFormName(new Concept(1, name, 0));
        return this;
    }

    public BahmniForm build() {
        return bahmniForm;
    }
}
