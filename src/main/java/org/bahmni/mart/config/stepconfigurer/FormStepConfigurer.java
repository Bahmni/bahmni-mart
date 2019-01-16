package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Configuration
public class FormStepConfigurer extends Form1StepConfigurer {

    private static final String ALL_FORMS = "All Observation Templates";
    private static final String TYPE = "obs";

    @Autowired
    public FormStepConfigurer(FormTableMetadataGenerator formTableMetadataGenerator) {
        super(formTableMetadataGenerator);
    }

    @Override
    protected List<BahmniForm> getAllForms() {
        JobDefinition jobDefinition = getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), TYPE);
        List<Concept> allFormConcepts = conceptService.getChildConcepts(ALL_FORMS, jobDefinition.getLocale());
        return formListProcessor.retrieveAllForms(allFormConcepts, jobDefinition);
    }
}
