package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Component
public class BacteriologyStepConfigurer extends StepConfigurer {

    private static final String BACTERIOLOGY_JOB_TYPE = "bacteriology";
    private static final String BACTERIOLOGY_CONCEPT_NAME = "Bacteriology Concept Set";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<Concept> allConcepts = conceptService
                .getConceptsByNames(Collections.singletonList(BACTERIOLOGY_CONCEPT_NAME));
        return formListProcessor.retrieveAllForms(allConcepts,
                getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), BACTERIOLOGY_JOB_TYPE));
    }
}
