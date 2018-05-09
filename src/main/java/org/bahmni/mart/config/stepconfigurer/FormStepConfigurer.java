package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class FormStepConfigurer extends StepConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(FormStepConfigurer.class);
    private static final String ALL_FORMS = "All Observation Templates";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<String> ignoreConcepts = JobDefinitionUtil
                .getIgnoreConceptNamesForObsJob(jobDefinitionReader.getJobDefinitions());
        List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
        List<BahmniForm> bahmniForms = formListProcessor.retrieveAllForms(allFormConcepts, ignoreConcepts);
        return filterFormsWithOutDuplicateConcepts(bahmniForms);
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
}
