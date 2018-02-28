package org.bahmni.mart.form;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FormFieldTransformer {

    public List<Integer> transformFormToFieldIds(BahmniForm form) {
        return form.getFields().stream().map(Concept::getId).collect(Collectors.toList());
    }


}
