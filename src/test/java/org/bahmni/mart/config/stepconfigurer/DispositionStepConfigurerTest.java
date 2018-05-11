package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
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
public class DispositionStepConfigurerTest {

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private ConceptService conceptService;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    private StepConfigurer dispositionStepConfigure;

    @Before
    public void setUp() throws Exception {
        dispositionStepConfigure = new DispositionStepConfigurer();
        setValuesForSuperClassMemberFields(dispositionStepConfigure, "jobDefinitionReader", jobDefinitionReader);
        setValuesForSuperClassMemberFields(dispositionStepConfigure, "formListProcessor", formListProcessor);
        setValuesForSuperClassMemberFields(dispositionStepConfigure, "conceptService", conceptService);
    }

    @Test
    public void shouldGetAllBacteriologyForms() throws Exception {
        List<String> ignoreConcepts = Collections.singletonList("image");
        List<Concept> allConcepts = Collections.singletonList(new Concept(1, "concept", 1));
        List<BahmniForm> forms = Collections.singletonList(new BahmniForm());
        JobDefinition dispositionJob = mock(JobDefinition.class);
        List<JobDefinition> jobDefinitions = Collections.singletonList(dispositionJob);
        String dispositionConceptName = "Disposition Set";
        List<String> conceptNames = Collections.singletonList(dispositionConceptName);
        String dispositionJobType = "disposition";
        when(dispositionJob.getType()).thenReturn(dispositionJobType);
        when(dispositionJob.getColumnsToIgnore()).thenReturn(Collections.emptyList());
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.getIgnoreConceptNamesForJob(dispositionJob)).thenReturn(ignoreConcepts);
        when(JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, dispositionJobType)).thenReturn(dispositionJob);
        when(conceptService.getConceptsByNames(conceptNames)).thenReturn(allConcepts);
        when(formListProcessor.retrieveAllForms(allConcepts, dispositionJob)).thenReturn(forms);

        List<BahmniForm> actual = dispositionStepConfigure.getAllForms();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(forms, actual);
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(conceptService, times(1)).getConceptsByNames(conceptNames);
        verify(formListProcessor, times(1)).retrieveAllForms(allConcepts, dispositionJob);
        verifyStatic(times(1));
        JobDefinitionUtil.getJobDefinitionByType(jobDefinitions, dispositionJobType);
    }
}