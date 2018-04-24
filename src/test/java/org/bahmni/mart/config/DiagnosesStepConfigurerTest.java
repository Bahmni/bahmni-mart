package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class DiagnosesStepConfigurerTest extends StepConfigurerTestHelper {

    private DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Before
    public void setUp() throws Exception {
        diagnosesStepConfigurer = new DiagnosesStepConfigurer();
        setUp(diagnosesStepConfigurer);
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