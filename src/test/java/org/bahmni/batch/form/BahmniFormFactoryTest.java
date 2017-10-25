package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.service.ObsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
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
	public void setup() throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		initMocks(this);

		addMoreAndMultiSelectConceptNames = "Operation Notes Template, Discharge Summary, Surgeries and Procedures, Other Notes, BP, Notes";
		List<Concept> addMoreAndMultiSelectConcepts = new ArrayList<>();
		addMoreAndMultiSelectConcepts.add(new Concept(3365,"Operation Notes Template",1));
		addMoreAndMultiSelectConcepts.add(new Concept(1200,"Discharge Summary, Surgeries and Procedures",1));
		addMoreAndMultiSelectConcepts.add(new Concept(1206,"Other Notes",1));
		addMoreAndMultiSelectConcepts.add(new Concept(7771,"BP",1));
		addMoreAndMultiSelectConcepts.add(new Concept(1209,"Notes",0));

		ignoreConceptNames = "Video, Audio";
		List<Concept> ignoreConcepts = new ArrayList<>();
		ignoreConcepts.add(new Concept(1111,"Video", 0));
		ignoreConcepts.add(new Concept(1112,"Audio", 0));

		allConcepts = new ArrayList<>();
		allConcepts.add(new Concept(1189,"History and Examination",1));
		allConcepts.add(new Concept(56	,"Vitals",1));
		allConcepts.add(new Concept(3365,"Operation Notes Template",1));

		historyAndExaminationConcepts = new ArrayList<>();
		historyAndExaminationConcepts.add(new Concept(1190,"Chief Complaint Data",	1));
		historyAndExaminationConcepts.add(new Concept(1194,"Chief Complaint Notes",	0));
		historyAndExaminationConcepts.add(new Concept(1843,"History",	0));
		historyAndExaminationConcepts.add(new Concept(1844,"Examination",	0));
		historyAndExaminationConcepts.add(new Concept(2077,"Image",	0));


		chiefComplaintDataConcepts = new ArrayList<>();
		chiefComplaintDataConcepts.add(new Concept(7771,"BP",1));

		vitalsConcepts = new ArrayList<>();
		vitalsConcepts.add(new Concept(1842,"Vitals Notes",0));

		operationNotesConcepts = new ArrayList<>();
		operationNotesConcepts.add(new Concept(3351,"Anesthesia Administered",0));
		operationNotesConcepts.add(new Concept(1206,"Other Notes",1));

		otherNotesConcepts = new ArrayList<>();
		otherNotesConcepts.add(new Concept(1209,"Notes",0));
		otherNotesConcepts.add(new Concept(1210,"Notes1",0));


		bahmniFormFactory = new BahmniFormFactory();
		bahmniFormFactory.setObsService(obsService);
		when(obsService.getConceptsByNames(addMoreAndMultiSelectConceptNames)).thenReturn(addMoreAndMultiSelectConcepts);
		when(obsService.getConceptsByNames(ignoreConceptNames)).thenReturn(ignoreConcepts);
        setValuesForMemberFields(bahmniFormFactory, "addMoreConceptNames", addMoreAndMultiSelectConceptNames);
        setValuesForMemberFields(bahmniFormFactory, "ignoreConceptsNames", ignoreConceptNames);
		bahmniFormFactory.postConstruct();
	}

    private void setValuesForMemberFields(Object bahmniFormFactory, String fieldName, String valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field f1 = bahmniFormFactory.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(bahmniFormFactory, valueForMemberField);
    }

    @Test
	public void createBahmniForm(){
		when(obsService.getChildConcepts("All Observation Templates")).thenReturn(allConcepts);
		when(obsService.getChildConcepts("History and Examination")).thenReturn(historyAndExaminationConcepts);
		when(obsService.getChildConcepts("Vitals")).thenReturn(vitalsConcepts);
		when(obsService.getChildConcepts("Operation Notes Template")).thenReturn(operationNotesConcepts);
		when(obsService.getChildConcepts("Chief Complaint Data")).thenReturn(chiefComplaintDataConcepts);
		when(obsService.getChildConcepts("Other Notes")).thenReturn(otherNotesConcepts);

		BahmniForm bahmniForm = bahmniFormFactory.createForm(new Concept(1,"History and Examination",1),null);

		assertNotNull(bahmniForm);
		assertEquals("History and Examination", bahmniForm.getFormName().getName());
		assertEquals("Chief Complaint Notes", bahmniForm.getFields().get(0).getName());
		assertEquals("History", bahmniForm.getFields().get(1).getName());
		assertEquals("Examination", bahmniForm.getFields().get(2).getName());
		assertEquals("Image", bahmniForm.getFields().get(3).getName());
		assertEquals(1,bahmniForm.getChildren().size());
		BahmniForm bpForm = bahmniForm.getChildren().get(0);
		assertEquals(bpForm.getDepthToParent(),2);


		bahmniForm = bahmniFormFactory.createForm(new Concept(1,"Vitals",1),null);
		assertNotNull(bahmniForm);
		assertEquals("Vitals", bahmniForm.getFormName().getName());
		assertEquals(vitalsConcepts, bahmniForm.getFields());

		bahmniForm = bahmniFormFactory.createForm(new Concept(1,"Operation Notes Template",1),null);
		assertNotNull(bahmniForm);

		assertEquals("Operation Notes Template", bahmniForm.getFormName().getName());
		assertEquals("Anesthesia Administered", bahmniForm.getFields().get(0).getName());
		assertEquals(1,bahmniForm.getChildren().size());

		BahmniForm otherNotesForm = bahmniForm.getChildren().get(0);
		assertEquals("Other Notes", otherNotesForm.getFormName().getName());
		assertEquals(otherNotesForm.getDepthToParent(),1);
		assertEquals(otherNotesForm.getChildren().size(),1);
		assertEquals(bahmniForm, otherNotesForm.getParent());
		assertEquals(1, otherNotesForm.getFields().size());
		assertEquals(1, otherNotesForm.getChildren().size());

		BahmniForm notesForm = otherNotesForm.getChildren().get(0);
		assertEquals(notesForm.getDepthToParent(),1);

		bahmniForm = bahmniFormFactory.createForm(new Concept(1,"Discharge Summary, Surgeries and Procedures",1),null);
		assertNotNull(bahmniForm);
		assertEquals(0, bahmniForm.getChildren().size());
		assertEquals(0, bahmniForm.getFields().size());

		assertNull(bahmniForm.getParent());

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
        when(obsService.getChildConcepts("Health Education")).thenReturn(Arrays.asList(hasTakenCourse, videoConcept));

        BahmniForm bahmniForm = bahmniFormFactory.createForm(healthEducation,null);

        assertNotNull(bahmniForm);
        assertEquals("Health Education", bahmniForm.getFormName().getName());
        assertEquals(1, bahmniForm.getFields().size());
        assertEquals(hasTakenCourse, bahmniForm.getFields().get(0));
        verify(obsService, times(1)).getConceptsByNames(addMoreAndMultiSelectConceptNames);
        verify(obsService, times(1)).getConceptsByNames(ignoreConceptNames);
    }
}
