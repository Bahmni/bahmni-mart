package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperSuperClassMemberFields;

public class StepConfigurerTestHelper {
    @Mock
    protected TableGeneratorStep tableGeneratorStep;

    @Mock
    protected FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    protected FormListProcessor formListProcessor;

    @Mock
    protected ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Mock
    protected ConceptService conceptService;

    @Mock
    protected JobDefinitionReader jobDefinitionReader;

    @Mock
    protected JobDefinition jobDefinition;

    @Mock
    protected Concept concept;

    protected void setUp(StepConfigurer classInstance) throws Exception {
        setValuesForSuperSuperClassMemberFields(classInstance, "tableGeneratorStep", tableGeneratorStep);
        setValuesForSuperSuperClassMemberFields(classInstance,
                "observationExportStepFactory", observationExportStepFactory);
        setValuesForSuperClassMemberFields(classInstance, "formListProcessor", formListProcessor);
        setValuesForSuperSuperClassMemberFields(classInstance, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperSuperClassMemberFields(classInstance, "conceptService", conceptService);
    }
}
