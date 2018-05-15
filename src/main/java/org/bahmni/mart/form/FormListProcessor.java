package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class FormListProcessor {

    @Autowired
    private BahmniFormFactory bahmniFormFactory;
    private static final Logger logger = LoggerFactory.getLogger(FormListProcessor.class);

    public List<BahmniForm> retrieveAllForms(List<Concept> formConcepts, JobDefinition jobDefinition) {
        List<String> ignoreConceptNames = JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        List<BahmniForm> forms = formConcepts.stream()
                .filter(concept -> !ignoreConceptNames.contains(concept.getName()))
                .map(concept -> bahmniFormFactory.createForm(concept, null, jobDefinition))
                .collect(Collectors.toList());

        List<BahmniForm> flattenedFormList = new ArrayList<>(forms);
        fetchExportFormsList(forms, flattenedFormList);
        //TODO: Refactor fetchExportFormsList not to change flattenedFormList inside instead it should return it
        return filterFormsWithOutDuplicateConcepts(flattenedFormList);
    }

    private List<BahmniForm> filterFormsWithOutDuplicateConcepts(List<BahmniForm> bahmniForms) {
        return bahmniForms.stream().filter(bahmniForm -> !hasDuplicateConcepts(bahmniForm))
                .collect(Collectors.toList());
    }

    private boolean hasDuplicateConcepts(BahmniForm bahmniForm) {
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
}
