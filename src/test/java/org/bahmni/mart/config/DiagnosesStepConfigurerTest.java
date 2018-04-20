package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class DiagnosesStepConfigurerTest {
    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Mock
    private ObsService obsService;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Concept concept;

    private DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Before
    public void setUp() throws Exception {
        diagnosesStepConfigurer = new DiagnosesStepConfigurer();
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer,
                "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer,
                "observationExportStepFactory", observationExportStepFactory);
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer, "formListProcessor", formListProcessor);
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(diagnosesStepConfigurer, "obsService", obsService);
    }

    @Test
    public void shouldReturnBahmniForms() {
        List<JobDefinition> jobDefinitions = Collections.singletonList(jobDefinition);
        List<Concept> concepts = Collections.singletonList(concept);

        mockStatic(JobDefinitionUtil.class);

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, "diagnoses")).thenReturn(jobDefinition);
        when(JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());
        when(obsService.getConceptsByNames(Collections.singletonList("Visit Diagnoses")))
                .thenReturn(concepts);
        when(formListProcessor.retrieveAllForms(concepts, Collections.emptyList())).thenReturn(Collections.emptyList());

        diagnosesStepConfigurer.getAllForms();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verifyStatic(times(1));
        JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, "diagnoses");
        verifyStatic(times(1));
        JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        verify(formListProcessor, times(1)).retrieveAllForms(concepts, Collections.emptyList());
        verify(obsService, times(1)).getConceptsByNames(Collections.singletonList("Visit Diagnoses"));
    }
}