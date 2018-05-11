package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.RspConfigHelper;
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

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;
import static org.bahmni.mart.table.FormTableMetadataGenerator.addPrefixToName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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

    @Mock
    private RspConfigHelper rspConfigHelper;

    @Before
    public void setUp() throws Exception {
        rspStepConfigurer = new RspStepConfigurer();
        setValuesForMemberFields(rspStepConfigurer, "rspConfigHelper", rspConfigHelper);
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
        when(getJobDefinitionByType(jobDefinitions, "rsp")).thenReturn(jobDefinition);
        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());
        when(rspConfigHelper.getRspConcepts()).thenReturn(conceptNames);
        when(conceptService.getConceptsByNames(conceptNames))
                .thenReturn(concepts);
        when(formListProcessor.retrieveAllForms(concepts, jobDefinition)).thenReturn(bahmniForms);
        when(concept.getName()).thenReturn("Nutritional Values");
        when(concept2.getName()).thenReturn("Fee Information");
        when(addPrefixToName("Nutritional Values", "rsp")).thenReturn("rsp Nutritional Values");
        when(addPrefixToName("Fee Information", "rsp")).thenReturn("rsp Fee Information");

        List<BahmniForm> actualForms = rspStepConfigurer.getAllForms();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verifyStatic(times(1));
        getJobDefinitionByType(jobDefinitions, "rsp");
        verify(formListProcessor, times(1)).retrieveAllForms(concepts, jobDefinition);
        verify(rspConfigHelper, times(1)).getRspConcepts();
        verify(conceptService, times(1)).getConceptsByNames(conceptNames);

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

    @Test
    public void shouldGiveEmptyListAsAllFormsIfRspConceptsIsMissing() {
        mockStatic(JobDefinitionUtil.class);

        List<JobDefinition> jobDefinitions = Collections.singletonList(jobDefinition);

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(getJobDefinitionByType(jobDefinitions, "rsp")).thenReturn(jobDefinition);
        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());
        when(rspConfigHelper.getRspConcepts()).thenReturn(Collections.emptyList());

        assertTrue(rspStepConfigurer.getAllForms().isEmpty());
        verify(conceptService, times(0)).getConceptsByNames(any());
        verify(formListProcessor, times(0)).retrieveAllForms(any(), any());
        verifyStatic(times(0));
        addPrefixToName(any(), any());
    }
}