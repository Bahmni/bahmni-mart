package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.table.FormTableMetadataGenerator.addPrefixToName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JobDefinitionUtil.class, FormTableMetadataGenerator.class})
public class RspStepConfigurerTest extends StepConfigurerTestHelper {

    private RspStepConfigurer rspStepConfigurer;

    @Mock
    private Concept concept2;

    @Before
    public void setUp() throws Exception {
        rspStepConfigurer = new RspStepConfigurer();
        setUp(rspStepConfigurer);
    }

    @Test
    public void shouldGetAllForms() {
        List<JobDefinition> jobDefinitions = Collections.singletonList(jobDefinition);
        List<Concept> concepts = Arrays.asList(concept, concept2);
        List<String> conceptNames = Arrays.asList("Nutritional Values", "Fee Information");

        BahmniForm nutritionalValues = new BahmniForm();
        nutritionalValues.setFormName(concept);

        BahmniForm feeInfo = new BahmniForm();
        feeInfo.setFormName(concept2);

        List<BahmniForm> bahmniForms = Arrays.asList(nutritionalValues, feeInfo);
        mockStatic(JobDefinitionUtil.class);
        mockStatic(FormTableMetadataGenerator.class);

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, "rsp")).thenReturn(jobDefinition);
        when(JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());
        when(obsService.getConceptsByNames(conceptNames))
                .thenReturn(concepts);
        when(formListProcessor.retrieveAllForms(concepts, Collections.emptyList())).thenReturn(bahmniForms);
        when(concept.getName()).thenReturn("Nutritional Values");
        when(concept2.getName()).thenReturn("Fee Information");
        when(addPrefixToName("Nutritional Values", "rsp")).thenReturn("rsp Nutritional Values");
        when(addPrefixToName("Fee Information", "rsp")).thenReturn("rsp Fee Information");

        List<BahmniForm> actualForms = rspStepConfigurer.getAllForms();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verifyStatic(times(1));
        JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, "rsp");
        verifyStatic(times(1));
        JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        verify(formListProcessor, times(1)).retrieveAllForms(concepts, Collections.emptyList());
        verify(obsService, times(1)).getConceptsByNames(conceptNames);

        assertEquals(2, actualForms.size());
        verifyStatic(times(1));
        addPrefixToName("Nutritional Values", "rsp");
        verifyStatic(times(1));
        addPrefixToName("Fee Information", "rsp");
        verify(concept, times(1)).getName();
        verify(concept2, times(1)).getName();
        verify(concept, times(1)).setName("rsp Nutritional Values");
        verify(concept2, times(1)).setName("rsp Fee Information");
    }
}