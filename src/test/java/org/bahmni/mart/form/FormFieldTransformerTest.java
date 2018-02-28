package org.bahmni.mart.form;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FormFieldTransformerTest {

    @Test
    public void shouldTransformFormFieldsToIds() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(0, "ParentConcept", 1));
        form.addField(new Concept(1, "headache", 0));
        form.addField(new Concept(2, "systolic", 0));
        form.addField(new Concept(3, "diastolic", 0));

        FormFieldTransformer formFieldTransformer = new FormFieldTransformer();
        List<Integer> ids = formFieldTransformer.transformFormToFieldIds(form);
        assertEquals(3, ids.size());
        assertEquals(new Integer(1), ids.get(0));
        assertEquals(new Integer(2), ids.get(1));
        assertEquals(new Integer(3), ids.get(2));
    }

}
