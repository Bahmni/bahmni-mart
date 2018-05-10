package org.bahmni.mart.helper;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class IgnoreColumnsConfigHelperTest {
    @Mock
    private ObsService obsService;

    @Mock
    private Concept concept;

    @Mock
    private Concept concept2;

    @Mock
    private Concept concept3;

    @Mock
    private JobDefinition jobDefinition;

    private IgnoreColumnsConfigHelper ignoreColumnsConfigHelper;

    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);

        ignoreColumnsConfigHelper = new IgnoreColumnsConfigHelper();
        setValuesForMemberFields(ignoreColumnsConfigHelper, "obsService", obsService);
    }

    @Test
    public void shouldGetAllTheIgnoreColumnsConceptsForGivenJob() {
        List<String> ignoreConceptNames = Arrays.asList("Image", "Video");
        List<Concept> expectedConcepts = Arrays.asList(concept, concept2);

        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(ignoreConceptNames);
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(Boolean.FALSE);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(ignoreConceptNames);
        when(obsService.getConceptsByNames(ignoreConceptNames)).thenReturn(expectedConcepts);

        List<Concept> ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);

        assertEquals(2, ignoreConcepts.size());
        assertEquals(expectedConcepts, ignoreConcepts);

        verify(jobDefinition, times(1)).getColumnsToIgnore();
        verify(jobDefinition, times(1)).getIgnoreAllFreeTextConcepts();
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
        verify(obsService, times(0)).getFreeTextConcepts();
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }

    @Test
    public void shouldAddAllFreeTextConceptsWithIgnoreConceptConfigAsIgnoreConceptsIfConfigIsTrue() {
        List<String> ignoreConceptNames = Collections.singletonList("Image");
        List<Concept> expectedConcepts = Arrays.asList(concept, concept2, concept3);

        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(ignoreConceptNames);
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(Boolean.TRUE);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(ignoreConceptNames);
        when(obsService.getConceptsByNames(ignoreConceptNames)).thenReturn(Collections.singletonList(concept));
        when(obsService.getFreeTextConcepts()).thenReturn(Arrays.asList(concept2, concept3));

        List<Concept> ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);

        assertEquals(3, ignoreConcepts.size());
        assertEquals(expectedConcepts, ignoreConcepts);

        verify(jobDefinition, times(1)).getColumnsToIgnore();
        verify(jobDefinition, times(1)).getIgnoreAllFreeTextConcepts();
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
        verify(obsService, times(1)).getFreeTextConcepts();
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }

    @Test
    public void shouldNotReEvaluateIgnoreConceptsIfItsDoneAlreadyForAParticularJob() {
        List<String> ignoreConceptNames = Collections.singletonList("Image");
        List<Concept> expectedConcepts = Arrays.asList(concept, concept2, concept3);

        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(ignoreConceptNames);
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(Boolean.TRUE);
        when(jobDefinition.getColumnsToIgnore()).thenReturn(ignoreConceptNames);
        when(obsService.getConceptsByNames(ignoreConceptNames)).thenReturn(Collections.singletonList(concept));
        when(obsService.getFreeTextConcepts()).thenReturn(Arrays.asList(concept2, concept3));

        List<Concept> ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);

        assertEquals(3, ignoreConcepts.size());
        assertEquals(expectedConcepts, ignoreConcepts);

        List<Concept> ignoreConceptsForJob = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);
        assertEquals(3, ignoreConceptsForJob.size());
        assertEquals(expectedConcepts, ignoreConceptsForJob);

        verify(jobDefinition, times(1)).getColumnsToIgnore();
        verify(jobDefinition, times(1)).getIgnoreAllFreeTextConcepts();
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
        verify(obsService, times(1)).getFreeTextConcepts();
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }

    @Test
    public void shouldAddAllFreeTextConceptsAsIgnoreConceptsIfFreeTextConfigIsTrueAndColumnsToIgnoreIsEmpty() {
        List<String> ignoreConceptNames = Collections.emptyList();
        List<Concept> expectedConcepts = Arrays.asList(concept2, concept3);

        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(ignoreConceptNames);
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(Boolean.TRUE);
        when(obsService.getFreeTextConcepts()).thenReturn(Arrays.asList(concept2, concept3));

        List<Concept> ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);

        assertEquals(2, ignoreConcepts.size());
        assertEquals(expectedConcepts, ignoreConcepts);

        verify(jobDefinition, times(0)).getColumnsToIgnore();
        verify(jobDefinition, times(1)).getIgnoreAllFreeTextConcepts();
        verify(obsService, times(0)).getConceptsByNames(ignoreConceptNames);
        verify(obsService, times(1)).getFreeTextConcepts();
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }

    @Test
    public void shouldGiveEmptyListIfConfigsAreNotSet() {
        when(getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(Collections.emptyList());
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(Boolean.FALSE);

        List<Concept> ignoreConcepts = ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition);
        assertTrue(ignoreConcepts.isEmpty());

        verify(jobDefinition, times(1)).getIgnoreAllFreeTextConcepts();
        verify(jobDefinition, times(0)).getColumnsToIgnore();
        verify(obsService, times(0)).getConceptsByNames(any());
        verify(obsService, times(0)).getFreeTextConcepts();
        verifyStatic(times(1));
        getIgnoreConceptNamesForJob(jobDefinition);
    }
}