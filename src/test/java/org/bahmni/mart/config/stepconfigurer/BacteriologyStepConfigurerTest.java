package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(JobDefinitionUtil.class)
@RunWith(PowerMockRunner.class)
public class BacteriologyStepConfigurerTest {

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private ConceptService conceptService;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    private StepConfigurer bacteriologyStepConfigurer;

    @Before
    public void setUp() throws Exception {
        bacteriologyStepConfigurer = new BacteriologyStepConfigurer();
        setValuesForSuperClassMemberFields(bacteriologyStepConfigurer, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(bacteriologyStepConfigurer, "formListProcessor", formListProcessor);
        setValuesForSuperClassMemberFields(bacteriologyStepConfigurer, "conceptService", conceptService);
    }

    @Test
    public void shouldGetAllBacteriologyForms() throws Exception {
        List<String> ignoreConcepts = Collections.singletonList("image");
        List<Concept> allConcepts = Collections.singletonList(new Concept(1, "concept", 1));
        List<BahmniForm> forms = Collections.singletonList(new BahmniForm());
        JobDefinition bacteriologyJob = mock(JobDefinition.class);
        List<JobDefinition> jobDefinitions = Collections.singletonList(bacteriologyJob);
        String bacteriologyConceptName = "Bacteriology Concept Set";
        List<String> conceptNames = Collections.singletonList(bacteriologyConceptName);
        String bacteriologyJobType = "bacteriology";
        when(bacteriologyJob.getType()).thenReturn(bacteriologyJobType);
        when(bacteriologyJob.getColumnsToIgnore()).thenReturn(ignoreConcepts);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, bacteriologyJobType)).thenReturn(bacteriologyJob);
        when(conceptService.getConceptsByNames(conceptNames)).thenReturn(allConcepts);
        when(formListProcessor.retrieveAllForms(allConcepts, bacteriologyJob)).thenReturn(forms);

        List<BahmniForm> actual = bacteriologyStepConfigurer.getAllForms();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(forms, actual);
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(conceptService, times(1)).getConceptsByNames(conceptNames);
        verify(formListProcessor, times(1)).retrieveAllForms(allConcepts, bacteriologyJob);
        verifyStatic(times(1));
        JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, bacteriologyJobType);
    }
}