package org.bahmni.mart.config;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Configuration
public class DiagnosesStepConfigurer extends ObsStepConfigurer {
    private static final String VISIT_DIAGNOSES = "Visit Diagnoses";
    private static final String VISIT_DIAGNOSES_TYPE = "diagnoses";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<String> ignoreConcepts = getIgnoreConceptNamesForJob(
                getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), VISIT_DIAGNOSES_TYPE));

        List<Concept> allFormConcepts = obsService.getConceptsByNames(Collections.singletonList(VISIT_DIAGNOSES));
        return formListProcessor.retrieveAllForms(allFormConcepts, ignoreConcepts);
    }
}
