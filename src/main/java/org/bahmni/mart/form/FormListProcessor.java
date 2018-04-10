package org.bahmni.mart.form;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FormListProcessor {

    @Autowired
    private BahmniFormFactory bahmniFormFactory;

    public List<BahmniForm> retrieveAllForms(List<Concept> formConcepts, List<String> ignoreConceptNames) {
        List<BahmniForm> forms = formConcepts.stream()
                .filter(concept -> !ignoreConceptNames.contains(concept.getName()))
                .map(concept -> bahmniFormFactory.createForm(concept, null)).collect(Collectors.toList());

        List<BahmniForm> flattenedFormList = new ArrayList<>(forms);
        fetchExportFormsList(forms, flattenedFormList);
        //TODO: Refactor fetchExportFormsList not to change flattenedFormList inside instead it should return it
        return flattenedFormList;
    }

    private void fetchExportFormsList(List<BahmniForm> forms, List<BahmniForm> flattenedList) {
        for (BahmniForm form : forms) {
            List<BahmniForm> children = form.getChildren();
            if (!children.isEmpty()) {
                flattenedList.addAll(children);
                fetchExportFormsList(children, flattenedList);
            }
        }
    }
}
