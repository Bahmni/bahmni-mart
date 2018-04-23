package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BacteriologyStepConfigurer extends StepConfigurer {

    private static final String BACTERIOLOGY_JOB_TYPE = "bacteriology";
    private static final String BACTERIOLOGY_CONCEPT_NAME = "Bacteriology Concept Set";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<String> ignoreConcepts = JobDefinitionUtil.getIgnoreConceptNamesForJob(JobDefinitionUtil
                .getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), BACTERIOLOGY_JOB_TYPE));
        List<Concept> allConcepts = obsService
                .getConceptsByNames(Collections.singletonList(BACTERIOLOGY_CONCEPT_NAME));
        return formListProcessor.retrieveAllForms(allConcepts, ignoreConcepts);
    }
}
