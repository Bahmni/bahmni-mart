package org.bahmni.mart.form;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.helper.IgnoreColumnsConfigHelper;
import org.bahmni.mart.helper.SeparateTableConfigHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({BatchUtils.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class BahmniFormFactoryTest {

    private List<Concept> historyAndExaminationConcepts;

    private List<Concept> chiefComplaintDataConcepts;

    private BahmniFormFactory bahmniFormFactory;

    @Mock
    private ConceptService conceptService;

    @Mock
    private JobDefinition obsJobDefinition;

    @Mock
    private JobDefinition bacteriologyJobDefinition;

    @Mock
    private SeparateTableConfigHelper separateTableConfigHelper;

    @Mock
    private IgnoreColumnsConfigHelper ignoreColumnsConfigHelper;

    private List<String> separateTableConceptList;
    private List<String> obsIgnoreConceptsNames;
    private List<String> bacteriologyIgnoreConceptsNames;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        separateTableConceptList = Arrays.asList("Operation Notes Template",
                "Discharge Summary, Surgeries and Procedures", "Other Notes", "BP", "Notes");
        List<Concept> separateTableConcepts = new ArrayList<>();
        separateTableConcepts.add(new Concept(3365, "Operation Notes Template", 1));
        separateTableConcepts.add(new Concept(1200, "Discharge Summary, Surgeries and Procedures", 1));
        separateTableConcepts.add(new Concept(1206, "Other Notes", 1));
        separateTableConcepts.add(new Concept(7771, "BP", 1));
        separateTableConcepts.add(new Concept(1209, "Notes", 0));

        obsIgnoreConceptsNames = Arrays.asList("Video", "Audio");
        bacteriologyIgnoreConceptsNames = Collections.singletonList("Image");
        List<Concept> ignoreConcepts = new ArrayList<>();
        ignoreConcepts.add(new Concept(1111, "Video", 0));
        ignoreConcepts.add(new Concept(1112, "Audio", 0));

        historyAndExaminationConcepts = new ArrayList<>();
        historyAndExaminationConcepts.add(new Concept(1190, "Chief Complaint Data", 1));
        historyAndExaminationConcepts.add(new Concept(1194, "Chief Complaint Notes", 0));
        historyAndExaminationConcepts.add(new Concept(1843, "History", 0));
        historyAndExaminationConcepts.add(new Concept(1844, "Examination", 0));
        historyAndExaminationConcepts.add(new Concept(2077, "Image", 0));

        chiefComplaintDataConcepts = new ArrayList<>();
        chiefComplaintDataConcepts.add(new Concept(7771, "BP", 1));

        bahmniFormFactory = new BahmniFormFactory();
        setValuesForMemberFields(bahmniFormFactory, "conceptService", conceptService);
        setValuesForMemberFields(bahmniFormFactory, "separateTableConfigHelper", separateTableConfigHelper);
        setValuesForMemberFields(bahmniFormFactory, "ignoreColumnsConfigHelper", ignoreColumnsConfigHelper);
        mockStatic(BatchUtils.class);
        mockStatic(JobDefinitionUtil.class);
        when(getIgnoreConceptNamesForJob(obsJobDefinition)).thenReturn(obsIgnoreConceptsNames);
        when(getIgnoreConceptNamesForJob(bacteriologyJobDefinition)).thenReturn(bacteriologyIgnoreConceptsNames);

        List<JobDefinition> jobDefinitions = Arrays.asList(obsJobDefinition, bacteriologyJobDefinition);
        when(getJobDefinitionByType(jobDefinitions, "obs")).thenReturn(obsJobDefinition);
        when(obsJobDefinition.isEmpty()).thenReturn(false);
        when(getJobDefinitionByType(jobDefinitions, "bacteriology")).thenReturn(bacteriologyJobDefinition);

        when(separateTableConfigHelper.getAllSeparateTableConceptNames()).thenReturn(separateTableConceptList);
        when(conceptService.getConceptsByNames(separateTableConceptList))
                .thenReturn(separateTableConcepts);
        when(conceptService.getConceptsByNames(obsIgnoreConceptsNames)).thenReturn(ignoreConcepts);
        when(conceptService.getFreeTextConcepts()).thenReturn(Collections.emptyList());
        when(obsJobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(true);
    }

    @Test
    public void shouldCreateFormWithNoFieldsAndChildren() {
        bahmniFormFactory.postConstruct();
        BahmniForm dischargeSummaryForm = bahmniFormFactory
                .createForm(new Concept(1, "Discharge Summary, Surgeries and Procedures", 1), null, obsJobDefinition);

        assertNotNull(dischargeSummaryForm);
        assertEquals(0, dischargeSummaryForm.getChildren().size());
        assertEquals(0, dischargeSummaryForm.getFields().size());

        assertNull(dischargeSummaryForm.getParent());
    }

    @Test
    public void shouldCreateAForm() {
        when(conceptService.getChildConcepts("History and Examination"))
                .thenReturn(historyAndExaminationConcepts);
        when(conceptService.getChildConcepts("Chief Complaint Data"))
                .thenReturn(chiefComplaintDataConcepts);

        bahmniFormFactory.postConstruct();

        BahmniForm historyAndExamination = bahmniFormFactory.createForm(
                new Concept(1189, "History and Examination", 1),
                null, obsJobDefinition);

        List<BahmniForm> historyAndExaminationChildren = historyAndExamination.getChildren();

        assertEquals("History and Examination", historyAndExamination.getFormName().getName());
        List<Concept> historyAndExaminationFields = historyAndExamination.getFields();
        assertEquals(4, historyAndExaminationFields.size());
        assertEquals("Chief Complaint Notes", historyAndExaminationFields.get(0).getName());
        assertEquals("History", historyAndExaminationFields.get(1).getName());
        assertEquals("Examination", historyAndExaminationFields.get(2).getName());
        assertEquals("Image", historyAndExaminationFields.get(3).getName());
        assertEquals(1, historyAndExaminationChildren.size());
        assertEquals(2, historyAndExaminationChildren.get(0).getDepthToParent());
        assertEquals("BP", historyAndExaminationChildren.get(0).getFormName().getName());
        verify(ignoreColumnsConfigHelper, times(1)).getIgnoreConceptsForJob(obsJobDefinition);
        verify(conceptService, times(1)).getConceptsByNames(separateTableConceptList);
    }

    @Test
    public void shouldIgnoreCreatingAFormWhenTheConceptNameIsInIgnoreConcepts() throws Exception {

        bahmniFormFactory.postConstruct();

        Concept healthEducation = new Concept(1110, "Health Education", 1);
        Concept videoConcept = new Concept(1111, "Video", 0);
        Concept hasTakenCourse = new Concept(1112, "Has patient taken course", 0);
        hasTakenCourse.setParent(healthEducation);
        videoConcept.setParent(healthEducation);
        when(conceptService.getChildConcepts("Health Education"))
                .thenReturn(Arrays.asList(hasTakenCourse, videoConcept));
        HashSet<Concept> ignoreConcepts = new HashSet<>();
        ignoreConcepts.add(videoConcept);
        when(ignoreColumnsConfigHelper.getIgnoreConceptsForJob(obsJobDefinition))
                .thenReturn(ignoreConcepts);

        BahmniForm bahmniForm = bahmniFormFactory.createForm(healthEducation, null, obsJobDefinition);

        assertNotNull(bahmniForm);
        assertEquals("Health Education", bahmniForm.getFormName().getName());
        assertEquals(1, bahmniForm.getFields().size());
        assertEquals(hasTakenCourse, bahmniForm.getFields().get(0));
        verify(conceptService, times(1)).getConceptsByNames(separateTableConceptList);
        verify(ignoreColumnsConfigHelper, times(1)).getIgnoreConceptsForJob(obsJobDefinition);
    }

    @Test
    public void shouldCreateChildForMultiSelectAddMoreAndSeparateTable() {

        historyAndExaminationConcepts.add(new Concept(3365, "Operation Notes Template", 1));
        historyAndExaminationConcepts.add(new Concept(1209, "Notes", 0));
        List<Concept> operationNotesTemplate = new ArrayList<>();
        operationNotesTemplate.add(new Concept(3366, "Notes Templates", 0));

        when(conceptService.getChildConcepts("History and Examination"))
                .thenReturn(historyAndExaminationConcepts);
        when(conceptService.getChildConcepts("Chief Complaint Data"))
                .thenReturn(chiefComplaintDataConcepts);
        when(conceptService.getChildConcepts("Operation Notes Template"))
                .thenReturn(operationNotesTemplate);
        when(conceptService.getChildConcepts("BP")).thenReturn(new ArrayList<>());

        bahmniFormFactory.postConstruct();

        BahmniForm historyAndExamination = bahmniFormFactory.createForm(
                new Concept(1189, "History and Examination", 1),
                null, obsJobDefinition);

        assertEquals("History and Examination", historyAndExamination.getFormName().getName());
        assertEquals(3, historyAndExamination.getChildren().size());
        assertEquals("BP", historyAndExamination.getChildren().get(0).getFormName().getName());
        assertEquals(0, historyAndExamination.getChildren().get(0).getFields().size());
        assertEquals("Operation Notes Template", historyAndExamination.getChildren().get(1).getFormName().getName());
        assertEquals(1, historyAndExamination.getChildren().get(1).getFields().size());
        assertEquals(1, historyAndExamination.getChildren().get(2).getFields().size());
        verify(conceptService, times(1)).getChildConcepts("History and Examination");
        verify(conceptService, times(1)).getChildConcepts("Chief Complaint Data");
        verify(conceptService, times(1)).getChildConcepts("Operation Notes Template");
        verify(conceptService, times(1)).getChildConcepts("BP");
        verify(ignoreColumnsConfigHelper, times(1)).getIgnoreConceptsForJob(obsJobDefinition);
    }

    @Test
    public void shouldReturnEmptySeparateTableListWhenThereIsNoObsJob() throws Exception {
        when(separateTableConfigHelper.getAllSeparateTableConceptNames()).thenReturn(Collections.emptyList());
        Field allSeparateTableConcepts = bahmniFormFactory.getClass().getDeclaredField("allSeparateTableConcepts");
        allSeparateTableConcepts.setAccessible(true);

        bahmniFormFactory.postConstruct();

        assertTrue(((List<Concept>) allSeparateTableConcepts.get(bahmniFormFactory)).isEmpty());
    }
}
