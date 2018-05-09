package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FormStepConfigurer extends StepConfigurer {

    private static final String ALL_FORMS = "All Observation Templates";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<String> ignoreConcepts = JobDefinitionUtil
                .getIgnoreConceptNamesForObsJob(jobDefinitionReader.getJobDefinitions());
        List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
        return formListProcessor.retrieveAllForms(allFormConcepts, ignoreConcepts);
    }
}
