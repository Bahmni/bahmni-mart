package org.bahmni.mart.form;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.bahmni.mart.helper.SeparateTableConfigHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({BatchUtils.class, JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class BahmniFormFactoryTest {

    private List<Concept> historyAndExaminationConcepts;

    private List<Concept> chiefComplaintDataConcepts;

    private BahmniFormFactory bahmniFormFactory;

    @Mock
    private ObsService obsService;

    @Mock
    private SeparateTableConfigHelper separateTableConfigHelper;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    private List<String> separateTableConceptList;
    private List<String> ignoreConceptsNameList;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        initMocks(this);

        separateTableConceptList = Arrays.asList("Operation Notes Template",
                "Discharge Summary, Surgeries and Procedures", "Other Notes", "BP", "Notes");
        List<Concept> separateTableConcepts = new ArrayList<>();
        separateTableConcepts.add(new Concept(3365, "Operation Notes Template", 1));
        separateTableConcepts.add(new Concept(1200, "Discharge Summary, Surgeries and Procedures", 1));
        separateTableConcepts.add(new Concept(1206, "Other Notes", 1));
        separateTableConcepts.add(new Concept(7771, "BP", 1));
        separateTableConcepts.add(new Concept(1209, "Notes", 0));

        ignoreConceptsNameList = Arrays.asList("Video", "Audio");
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
        bahmniFormFactory.setObsService(obsService);
        setValuesForMemberFields(bahmniFormFactory, "separateTableConfigHelper", separateTableConfigHelper);
        setValuesForMemberFields(bahmniFormFactory, "jobDefinitionReader", jobDefinitionReader);
        mockStatic(BatchUtils.class);
        mockStatic(JobDefinitionUtil.class);
        when(obsService.getConceptsByNames(any())).thenReturn(ignoreConcepts);
        when(JobDefinitionUtil.getIgnoreConceptNamesForObsJob(any())).thenReturn(ignoreConceptsNameList);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList());
        when(separateTableConfigHelper.getAllSeparateTableConceptNames()).thenReturn(separateTableConceptList);
        when(obsService.getConceptsByNames(separateTableConceptList))
                .thenReturn(separateTableConcepts);
        when(obsService.getConceptsByNames(ignoreConceptsNameList)).thenReturn(ignoreConcepts);
        bahmniFormFactory.postConstruct();
    }

    @Test
    public void shouldCreateFormWithNoFieldsAndChildren() {
        BahmniForm dischargeSummaryForm = bahmniFormFactory
                .createForm(new Concept(1, "Discharge Summary, Surgeries and Procedures", 1), null);

        assertNotNull(dischargeSummaryForm);
        assertEquals(0, dischargeSummaryForm.getChildren().size());
        assertEquals(0, dischargeSummaryForm.getFields().size());

        assertNull(dischargeSummaryForm.getParent());
    }

    @Test
    public void shouldCreateAForm() {
        when(obsService.getChildConcepts("History and Examination"))
                .thenReturn(historyAndExaminationConcepts);
        when(obsService.getChildConcepts("Chief Complaint Data"))
                .thenReturn(chiefComplaintDataConcepts);

        BahmniForm historyAndExamination = bahmniFormFactory.createForm(
                new Concept(1189, "History and Examination", 1),
                null);

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
        verify(obsService, times(1)).getConceptsByNames(separateTableConceptList);
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptsNameList);
    }

    @Test
    public void shouldIgnoreCreatingAFormWhenTheConceptNameIsInIgnoreConcepts() throws Exception {
        Concept healthEducation = new Concept(1110, "Health Education", 1);
        Concept videoConcept = new Concept(1111, "Video", 0);
        Concept hasTakenCourse = new Concept(1112, "Has patient taken course", 0);
        hasTakenCourse.setParent(healthEducation);
        videoConcept.setParent(healthEducation);
        when(obsService.getChildConcepts("Health Education"))
                .thenReturn(Arrays.asList(hasTakenCourse, videoConcept));

        BahmniForm bahmniForm = bahmniFormFactory.createForm(healthEducation, null);

        assertNotNull(bahmniForm);
        assertEquals("Health Education", bahmniForm.getFormName().getName());
        assertEquals(1, bahmniForm.getFields().size());
        assertEquals(hasTakenCourse, bahmniForm.getFields().get(0));
        verify(obsService, times(1)).getConceptsByNames(separateTableConceptList);
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptsNameList);
    }

    @Test
    public void shouldCreateChildForMultiSelectAddMoreAndSeparateTable() {
        historyAndExaminationConcepts.add(new Concept(3365, "Operation Notes Template", 1));
        historyAndExaminationConcepts.add(new Concept(1209, "Notes", 0));
        List<Concept> operationNotesTemplate = new ArrayList<>();
        operationNotesTemplate.add(new Concept(3366, "Notes Templates", 0));

        when(obsService.getChildConcepts("History and Examination"))
                .thenReturn(historyAndExaminationConcepts);
        when(obsService.getChildConcepts("Chief Complaint Data"))
                .thenReturn(chiefComplaintDataConcepts);
        when(obsService.getChildConcepts("Operation Notes Template"))
                .thenReturn(operationNotesTemplate);
        when(obsService.getChildConcepts("BP")).thenReturn(new ArrayList<>());


        BahmniForm historyAndExamination = bahmniFormFactory.createForm(
                new Concept(1189, "History and Examination", 1),
                null);

        assertEquals("History and Examination", historyAndExamination.getFormName().getName());
        assertEquals(3, historyAndExamination.getChildren().size());
        assertEquals("BP", historyAndExamination.getChildren().get(0).getFormName().getName());
        assertEquals(0, historyAndExamination.getChildren().get(0).getFields().size());
        assertEquals("Operation Notes Template", historyAndExamination.getChildren().get(1).getFormName().getName());
        assertEquals(1, historyAndExamination.getChildren().get(1).getFields().size());
        assertEquals(1, historyAndExamination.getChildren().get(2).getFields().size());
        verify(obsService, times(1)).getChildConcepts("History and Examination");
        verify(obsService, times(1)).getChildConcepts("Chief Complaint Data");
        verify(obsService, times(1)).getChildConcepts("Operation Notes Template");
        verify(obsService, times(1)).getChildConcepts("BP");
    }
}
