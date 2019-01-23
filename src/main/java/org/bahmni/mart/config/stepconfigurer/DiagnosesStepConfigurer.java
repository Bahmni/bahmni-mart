package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Configuration
public class DiagnosesStepConfigurer extends Form1StepConfigurer {
    private static final String VISIT_DIAGNOSES = "Visit Diagnoses";
    private static final String VISIT_DIAGNOSES_TYPE = "diagnoses";

    @Autowired
    public DiagnosesStepConfigurer(FormTableMetadataGenerator formTableMetadataGenerator) {
        super(formTableMetadataGenerator);
    }

    @Override
    protected List<BahmniForm> getAllForms() {
        List<Concept> allFormConcepts = conceptService.getConceptsByNames(Collections.singletonList(VISIT_DIAGNOSES));
        return formListProcessor.retrieveAllForms(allFormConcepts,
                getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), VISIT_DIAGNOSES_TYPE));
    }
}
