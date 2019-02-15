package org.bahmni.mart.helper;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.helper.FormListHelper.filterFormsWithOutDuplicateSectionsAndConcepts;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FormListHelperTest {

    @Mock
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        setValueForFinalStaticField(FormListHelper.class, "logger", logger);
    }

    @Test
    public void shouldFilterDuplicateConceptsThroughOutTheForm() {
        Concept concept1 = new Concept(1, "concept1", 0);
        Concept concept2 = new Concept(2, "concept2", 0);
        Concept concept3 = new Concept(3, "concept3", 0);
        Concept concept4 = new Concept(4, "concept4", 0);
        Concept form1Concept = new Concept(5, "form1", 0);
        Concept form2Concept = new Concept(6, "form2", 0);
        Concept form3Concept = new Concept(7, "form3", 0);
        Concept form1ChildFormConcept = new Concept(8, "form1ChildForm", 0);
        Concept form1ChildChildFormConcept = new Concept(9, "form1ChildChildForm", 0);
        Concept form3ChildFormConcept = new Concept(10, "form3ChildForm", 0);

        BahmniForm form1 = new BahmniForm();
        BahmniForm form2 = new BahmniForm();
        BahmniForm form3 = new BahmniForm();

        BahmniForm form1ChildForm = new BahmniForm();
        BahmniForm form1ChildChildForm = new BahmniForm();
        BahmniForm form3ChildForm = new BahmniForm();

        form1ChildChildForm.addField(concept1);
        form1ChildChildForm.addField(concept4);
        form1ChildChildForm.setFormName(form1ChildChildFormConcept);
        form1ChildForm.addChild(form1ChildChildForm);
        form1ChildForm.setFormName(form1ChildFormConcept);
        form1ChildForm.addField(concept3);
        form1.addChild(form1ChildForm);
        form1.addField(concept1);
        form1.addField(concept2);
        form1.setFormName(form1Concept);

        form2.addField(concept1);
        form2.addField(concept2);
        form2.addField(concept1);
        form2.setFormName(form2Concept);

        form3ChildForm.addField(concept3);
        form3ChildForm.setFormName(form3ChildFormConcept);
        form3.addChild(form3ChildForm);
        form3.addField(concept1);
        form3.addField(concept2);
        form3.setFormName(form3Concept);

        List<BahmniForm> bahmniForms = Arrays.asList(form1, form2, form3);

        List<BahmniForm> filteredForms = filterFormsWithOutDuplicateSectionsAndConcepts(bahmniForms);

        assertEquals(1, filteredForms.size());
        assertEquals(form3, filteredForms.get(0));
    }

    @Test
    public void shouldLogSkippingMessageWhenFormHavingDuplicateConcept() throws Exception {
        Concept concept1 = new Concept(1, "concept1", 0);
        Concept concept2 = new Concept(2, "concept2", 0);
        Concept concept3 = new Concept(3, "concept3", 0);
        Concept concept4 = new Concept(4, "concept4", 0);
        Concept formConcept = new Concept(5, "form", 0);
        Concept formChildForm1Concept = new Concept(6, "formChildForm1", 0);
        Concept formChildForm2Concept = new Concept(7, "formChildForm2", 0);

        BahmniForm form = new BahmniForm();
        BahmniForm formChildForm1 = new BahmniForm();
        BahmniForm formChildForm2 = new BahmniForm();

        formChildForm1.addField(concept2);
        formChildForm1.addField(concept3);
        formChildForm1.setFormName(formChildForm1Concept);
        form.addChild(formChildForm1);
        formChildForm2.addField(concept2);
        formChildForm2.addField(concept4);
        formChildForm2.setFormName(formChildForm2Concept);
        form.setFormName(formConcept);
        form.addChild(formChildForm2);
        form.addField(concept1);

        List<BahmniForm> filteredForms = filterFormsWithOutDuplicateSectionsAndConcepts(singletonList(form));

        assertEquals(0, filteredForms.size());
        verify(logger).warn("Skipping the form 'form' since it has duplicate concepts 'concept2'");
    }

    @Test
    public void shouldFilterDuplicateSectionsThroughOutTheForm() {
        Concept concept1 = new Concept(1, "concept1", 0);
        Concept concept2 = new Concept(2, "concept2", 0);
        Concept concept3 = new Concept(3, "concept3", 0);
        Concept concept4 = new Concept(4, "concept4", 0);
        Concept formConcept = new Concept(5, "form", 0);
        Concept form2Concept = new Concept(6, "form2", 0);
        Concept sectionConcept1 = new Concept(7, "test section 1", 0);
        Concept sectionConcept2 = new Concept(8, "test section 2", 0);

        BahmniForm form = new BahmniForm();
        BahmniForm formChildForm1 = new BahmniForm();
        BahmniForm formChildForm2 = new BahmniForm();
        BahmniForm formChildForm3 = new BahmniForm();

        formChildForm3.addField(concept3);
        formChildForm3.setFormName(sectionConcept1);

        formChildForm2.addField(concept2);
        formChildForm2.addChild(formChildForm3);
        formChildForm2.setFormName(sectionConcept2);

        formChildForm1.addField(concept1);
        formChildForm1.addChild(formChildForm2);
        formChildForm1.setFormName(sectionConcept1);

        form.addChild(formChildForm1);
        form.setFormName(formConcept);
        form.addField(concept4);

        BahmniForm form2 = new BahmniForm();
        BahmniForm form2ChildForm1 = new BahmniForm();
        BahmniForm form2ChildForm2 = new BahmniForm();
        form2ChildForm1.setFormName(sectionConcept1);
        form2ChildForm1.addField(concept1);
        form2ChildForm2.setFormName(sectionConcept1);
        form2ChildForm2.addField(concept2);
        form2.addChild(form2ChildForm1);
        form2.addChild(form2ChildForm2);
        form2.addField(concept3);
        form2.setFormName(form2Concept);

        List<BahmniForm> filteredForms = filterFormsWithOutDuplicateSectionsAndConcepts(Arrays.asList(form, form2));

        assertEquals(0, filteredForms.size());
        verify(logger).warn("Skipping the form 'form' since it has duplicate sections 'test section 1'");
    }
}
