package org.bahmni.mart.form;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class BahmniFormFactoryTest {

    private List<Concept> allConcepts;

    private List<Concept> historyAndExaminationConcepts;

    private List<Concept> vitalsConcepts;

    private List<Concept> chiefComplaintDataConcepts;

    private List<Concept> operationNotesConcepts;

    private List<Concept> otherNotesConcepts;

    private BahmniFormFactory bahmniFormFactory;

    @Mock
    private ObsService obsService;

    private String addMoreAndMultiSelectConceptNames;
    private String ignoreConceptNames;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        initMocks(this);

        addMoreAndMultiSelectConceptNames = "Operation Notes Template, Discharge Summary," +
                " Surgeries and Procedures, Other Notes, BP, Notes";
        List<Concept> addMoreAndMultiSelectConcepts = new ArrayList<>();
        addMoreAndMultiSelectConcepts.add(new Concept(3365, "Operation Notes Template", 1));
        addMoreAndMultiSelectConcepts.add(new Concept(1200, "Discharge Summary, Surgeries and Procedures", 1));
        addMoreAndMultiSelectConcepts.add(new Concept(1206, "Other Notes", 1));
        addMoreAndMultiSelectConcepts.add(new Concept(7771, "BP", 1));
        addMoreAndMultiSelectConcepts.add(new Concept(1209, "Notes", 0));

        ignoreConceptNames = "Video, Audio";
        List<Concept> ignoreConcepts = new ArrayList<>();
        ignoreConcepts.add(new Concept(1111, "Video", 0));
        ignoreConcepts.add(new Concept(1112, "Audio", 0));

        allConcepts = new ArrayList<>();
        allConcepts.add(new Concept(1189, "History and Examination", 1));
        allConcepts.add(new Concept(56, "Vitals", 1));
        allConcepts.add(new Concept(3365, "Operation Notes Template", 1));

        historyAndExaminationConcepts = new ArrayList<>();
        historyAndExaminationConcepts.add(new Concept(1190, "Chief Complaint Data", 1));
        historyAndExaminationConcepts.add(new Concept(1194, "Chief Complaint Notes", 0));
        historyAndExaminationConcepts.add(new Concept(1843, "History", 0));
        historyAndExaminationConcepts.add(new Concept(1844, "Examination", 0));
        historyAndExaminationConcepts.add(new Concept(2077, "Image", 0));


        chiefComplaintDataConcepts = new ArrayList<>();
        chiefComplaintDataConcepts.add(new Concept(7771, "BP", 1));

        vitalsConcepts = new ArrayList<>();
        vitalsConcepts.add(new Concept(1842, "Vitals Notes", 0));

        operationNotesConcepts = new ArrayList<>();
        operationNotesConcepts.add(new Concept(3351, "Anesthesia Administered", 0));
        operationNotesConcepts.add(new Concept(1206, "Other Notes", 1));

        otherNotesConcepts = new ArrayList<>();
        otherNotesConcepts.add(new Concept(1209, "Notes", 0));
        otherNotesConcepts.add(new Concept(1210, "Notes1", 0));


        bahmniFormFactory = new BahmniFormFactory();
        bahmniFormFactory.setObsService(obsService);
        when(obsService.getConceptsByNames(addMoreAndMultiSelectConceptNames))
                .thenReturn(addMoreAndMultiSelectConcepts);
        when(obsService.getConceptsByNames(ignoreConceptNames)).thenReturn(ignoreConcepts);
        setValuesForMemberFields(bahmniFormFactory, "addMoreConceptNames", addMoreAndMultiSelectConceptNames);
        setValuesForMemberFields(bahmniFormFactory, "ignoreConceptsNames", ignoreConceptNames);
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
    @Ignore // This test is about create new table per concept-set. But currently we are creating table per form
    public void shouldCreatAForm() {
        when(obsService.getChildConcepts("All Observation Templates")).thenReturn(allConcepts);
        when(obsService.getChildConcepts("History and Examination"))
                .thenReturn(historyAndExaminationConcepts);
        when(obsService.getChildConcepts("Vitals")).thenReturn(vitalsConcepts);
        when(obsService.getChildConcepts("Operation Notes Template"))
                .thenReturn(operationNotesConcepts);
        when(obsService.getChildConcepts("Chief Complaint Data"))
                .thenReturn(chiefComplaintDataConcepts);
        when(obsService.getChildConcepts("Other Notes")).thenReturn(otherNotesConcepts);

        BahmniForm allObservationTemplates = bahmniFormFactory
                .createForm(new Concept(1,"All Observation Templates",1),null);

        assertNotNull(allObservationTemplates);

        assertEquals("All Observation Templates",allObservationTemplates.getFormName().getName());
        assertEquals(3,allObservationTemplates.getChildren().size());

        List<String> children = allObservationTemplates.getChildren().stream().map(form -> form.getFormName().getName())
                .collect(Collectors.toList());
        assertTrue(children.containsAll(
                Arrays.asList("History and Examination", "Vitals", "Operation Notes Template")));

        assertNull(allObservationTemplates.getRootForm());

        BahmniForm historyAndExaminationForm = allObservationTemplates.getChildren().get(0);
        assertEquals(1,historyAndExaminationForm.getDepthToParent());
        assertEquals(1,historyAndExaminationForm.getChildren().size());
        assertEquals(4,historyAndExaminationForm.getFields().size());

        assertEquals("Chief Complaint Notes",historyAndExaminationForm.getFields().get(0).getName());
        assertEquals(allObservationTemplates,historyAndExaminationForm.getParent());
        BahmniForm chiefComplaintNotesForm = historyAndExaminationForm.getChildren().get(0);
        assertEquals(historyAndExaminationForm, chiefComplaintNotesForm.getRootForm());
        assertEquals(2,chiefComplaintNotesForm.getDepthToParent());

        BahmniForm vitalsForm = allObservationTemplates.getChildren().get(1);
        assertEquals(1, vitalsForm.getFields().size());
        assertTrue(vitalsForm.getChildren().isEmpty());
        assertEquals("Vitals Notes", vitalsForm.getFields().get(0).getName());
        assertEquals(1, vitalsForm.getDepthToParent());
        assertNull(vitalsForm.getRootForm());
        assertEquals(allObservationTemplates, vitalsForm.getParent());

        BahmniForm operationNotesTemplateForm = allObservationTemplates.getChildren().get(2);
        assertEquals(1, operationNotesTemplateForm.getFields().size());
        assertEquals(1, operationNotesTemplateForm.getChildren().size());
        assertEquals("Anesthesia Administered", operationNotesTemplateForm.getFields().get(0).getName());
        assertEquals("Other Notes", operationNotesTemplateForm.getChildren().get(0).getFormName().getName());
        assertEquals(1, operationNotesTemplateForm.getDepthToParent());
        assertNull(operationNotesTemplateForm.getRootForm());
        assertEquals(allObservationTemplates, operationNotesTemplateForm.getParent());

        assertEquals(2, operationNotesTemplateForm.getChildren().get(0).getDepthToParent());
        assertEquals(operationNotesTemplateForm, operationNotesTemplateForm.getChildren().get(0).getRootForm());

        verify(obsService, times(1)).getConceptsByNames(addMoreAndMultiSelectConceptNames);
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
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
        verify(obsService, times(1)).getConceptsByNames(addMoreAndMultiSelectConceptNames);
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
    }
}
