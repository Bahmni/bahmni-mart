package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractFormListProcessor {
    private List<BahmniForm> filterFormsWithOutDuplicateConcepts(List<BahmniForm> bahmniForms, Logger logger) {
        return bahmniForms.stream().filter(bahmniForm -> !hasDuplicateConcepts(bahmniForm, logger))
                .collect(Collectors.toList());
    }

    private boolean hasDuplicateConcepts(BahmniForm bahmniForm, Logger logger) {
        if (bahmniForm != null) {
            Set<String> concepts = new HashSet<>();
            for (Concept concept : bahmniForm.getFields()) {
                if (!concepts.add(concept.getName())) {
                    logger.warn(String.format("Skipping the form '%s' since it has duplicate concepts '%s'",
                            bahmniForm.getFormName().getName(), concept.getName()));
                    return true;
                }
            }
        }
        return false;
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

    protected List<BahmniForm> getUniqueFlattenedBahmniForms(List<BahmniForm> forms, Logger logger) {
        List<BahmniForm> flattenedFormList = new ArrayList<>(forms);
        fetchExportFormsList(forms, flattenedFormList);
        return filterFormsWithOutDuplicateConcepts(flattenedFormList, logger);
    }

    public abstract List<BahmniForm> retrieveAllForms(List<Concept> formConcepts, JobDefinition jobDefinition);
}
