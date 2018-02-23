package org.bahmni.analytics.form;

import org.bahmni.analytics.form.domain.BahmniForm;
import org.bahmni.analytics.form.domain.Concept;
import org.bahmni.analytics.form.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FormListProcessor {

    public static final String ALL_FORMS = "All Observation Templates";

    @Autowired
    private ObsService obsService;

    @Autowired
    private BahmniFormFactory bahmniFormFactory;


    public List<BahmniForm> retrieveAllForms() {
        List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
        List<BahmniForm> forms = allFormConcepts.stream()
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

    public void setObsService(ObsService obsService) {
        this.obsService = obsService;
    }

    public void setBahmniFormFactory(BahmniFormFactory bahmniFormFactory) {
        this.bahmniFormFactory = bahmniFormFactory;
    }
}
