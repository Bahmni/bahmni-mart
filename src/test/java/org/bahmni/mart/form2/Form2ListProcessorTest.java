package org.bahmni.mart.form2;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.bahmni.mart.form2.uitl.Form2MetadataReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class Form2ListProcessorTest {
    private static final String FORM_PATH = "SomeForm.json";
    private static final String COMPLEX_FORM = "ComplextForm";
    private Form2ListProcessor form2ListProcessor;
    private Map<String, String> allForms = new HashMap<String, String>();

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Form2MetadataReader form2MetadataReader;

    @Before
    public void setUp() throws Exception {
        form2ListProcessor = new Form2ListProcessor();
        allForms.put(COMPLEX_FORM, FORM_PATH);
        setValuesForMemberFields(form2ListProcessor, "form2MetadataReader", form2MetadataReader);
        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldsWhenFormHasOnlyObsControl() {
        final String obsConceptName = "ObsConcept";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(singletonList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        assertEquals(bahmniForm.getChildren().size(), 0);
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(bahmniFormFields.size(), 1);
        assertEquals(bahmniFormFields.get(0).getName(), obsConceptName);
    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenWhenFormHasObsControlWithAddMore() {
        final String obsConceptName = "ObsConcept";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(singletonList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        assertEquals(bahmniForm.getFields().size(), 0);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniFormChildren.size(), 1);
        assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName);
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldAsObsConceptWhenFormHasObsInsideSection() {
        final String obsConceptName = "ObsConcept";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(singletonList(obsControl))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(bahmniFormFields.size(), 1);
        assertEquals(bahmniFormFields.get(0).getName(), obsConceptName);
    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenAsObsConceptWhenFormHasObsWithAddMoreInsideSection() {
        final String obsConceptName = "ObsConcept";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(singletonList(obsControl))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniForm.getFields().size(), 0);
        assertEquals(bahmniFormChildren.size(), 1);
        assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName);
    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWithObsConceptAsChildWhenSectionAddMoreContainsObsAddMore() {
        final String obsConceptName = "ObsConcept";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(singletonList(obsControl))
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniForm.getFields().size(), 0);
        assertEquals(bahmniFormChildren.size(), 1);
        assertEquals(bahmniFormChildren.get(0).getFormName().getName(), "Section");
        assertEquals(bahmniFormChildren.get(0).getFields().size(), 0);
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        assertEquals(sectionChildren.size(), 1);
        assertEquals(sectionChildren.get(0).getFormName().getName(), obsConceptName);
        assertEquals(1, sectionChildren.get(0).getFields().size());
        assertEquals(obsConceptName, sectionChildren.get(0).getFields().get(0).getName());
    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWith2ObsConceptAsChildrenWhenSectionAddMoreContains2ObsAddMore() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        assertNull(bahmniForm.getRootForm());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniForm.getFields().size(), 0);
        assertEquals(bahmniFormChildren.size(), 1);
        assertEquals(bahmniFormChildren.get(0).getFormName().getName(), "Section");
        assertEquals(bahmniFormChildren.get(0).getFields().size(), 0);
        assertEquals(bahmniForm, bahmniFormChildren.get(0).getRootForm());
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        assertEquals(sectionChildren.size(), 2);
        assertEquals(sectionChildren.get(0).getFormName().getName(), obsConceptName1);
        assertEquals(sectionChildren.get(1).getFormName().getName(), obsConceptName2);
        assertEquals(2, sectionChildren.get(0).getDepthToParent());
        assertEquals(2, sectionChildren.get(1).getDepthToParent());
        assertEquals(bahmniForm, sectionChildren.get(0).getRootForm());
        assertEquals(bahmniForm, sectionChildren.get(1).getRootForm());
    }

    @Test
    public void shouldReturnBahmniFormWith2ObsConceptAsChildrenWhenSectionContains2ObsAddMore() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniForm.getFields().size(), 0);
        assertEquals(bahmniFormChildren.size(), 2);
        assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName1);
        assertEquals(bahmniFormChildren.get(1).getFormName().getName(), obsConceptName2);
        assertEquals(1, bahmniFormChildren.get(0).getDepthToParent());
        assertEquals(1, bahmniFormChildren.get(1).getDepthToParent());
    }

    @Test
    public void shouldReturnBahmniFormWithTableObsWhenFormContainsATable() {
        final String obsConceptName1 = "ObsConcept1";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control emptyControl1 = new ControlBuilder().build();
        Control emptyControl2 = new ControlBuilder().build();
        Control tableControl = new ControlBuilder()
                .withControls(Arrays.asList(emptyControl1, emptyControl2, obsControl))
                .withPropertyAddMore(false)
                .withLabel("Table")
                .build();

        form2JsonMetadata.setControls(singletonList(tableControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(bahmniForm.getFormName().getName(), COMPLEX_FORM);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(bahmniForm.getFields().size(), 1);
        assertEquals(bahmniFormChildren.size(), 0);
    }

    @Test
    public void shouldFilterOutFormsWithDuplicateConcepts() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control sectionControl1 = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(obsControl1, obsControl1, obsControl2))
                .withPropertyAddMore(false)
                .build();
        Control sectionControl2 = new ControlBuilder()
                .withLabel("Section")
                .withControls(singletonList(obsControl2))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(sectionControl1, sectionControl2));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 0);
    }

    @Test
    public void shouldFilterOutConceptsGivenInIgnoreConceptNames() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control sectionControl1 = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withPropertyAddMore(false)
                .build();
        when(JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition)).thenReturn(singletonList(obsConceptName2));
        form2JsonMetadata.setControls(singletonList(sectionControl1));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final List<Concept> fields = allForms.get(0).getFields();
        assertEquals(1, fields.size());
        assertEquals(fields.get(0).getName(), obsConceptName1);
    }

    @Test
    public void shouldNotAddChildrenForAddMoreAndMultiSelectWhenEnableAddMoreAndMultiSelectIsFalse() {
        String multiSelectConceptName = "multiSelectObsConcept";
        Control multiSelectObsControl = new ControlBuilder()
                .withPropertyMultiSelect(true)
                .withConcept(multiSelectConceptName, "obs_concept_1").build();
        String addMoreConceptName = "addMoreObsConcept";
        Control addMoreObsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(addMoreConceptName, "obs_concept_2").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(Arrays.asList(multiSelectObsControl, addMoreObsControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        List<Concept> fields = allForms.get(0).getFields();
        assertEquals(0, allForms.get(0).getChildren().size());
        assertEquals(2, fields.size());
        assertEquals(multiSelectConceptName, fields.get(0).getName());
        assertEquals(addMoreConceptName, fields.get(1).getName());
    }

    @Test
    public void shouldSetIsSectionToTrueWhenControlIsASection() {
        final String obsConceptName1 = "ObsConcept1";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control sectionControl1 = new ControlBuilder()
                .withLabel("Section")
                .withType("Section")
                .withControls(Arrays.asList(obsControl1))
                .withPropertyAddMore(true)
                .build();

        form2JsonMetadata.setControls(singletonList(sectionControl1));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        assertEquals(COMPLEX_FORM  + " Section", allForms.get(0).getChildren().get(0).getFormName().getName());
    }

    @Test
    public void shouldReturnBahmniFormHavingSectionWith2NestedSectionsAndTheFirstInnerSectionIsAddMoreWithObs() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String obsConceptName3 = "ObsConcept3";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control obsControl3 = new ControlBuilder()
                .withConcept(obsConceptName3, "obs_concept_3").build();
        Control mostInnerSectionControl = new ControlBuilder()
                .withLabel("Section11")
                .withControls(Collections.singletonList(obsControl3))
                .withPropertyAddMore(true)
                .build();
        Control innerSectionControl = new ControlBuilder()
                .withLabel("Section1")
                .withControls(Arrays.asList(obsControl2, mostInnerSectionControl))
                .withPropertyAddMore(false)
                .build();
        Control sectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(obsControl1, innerSectionControl))
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertNull(bahmniForm.getRootForm());
        assertEquals(0, bahmniForm.getFields().size());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(1, bahmniFormChildren.size());
        BahmniForm sectionForm = bahmniFormChildren.get(0);
        assertEquals("Section", sectionForm.getFormName().getName());
        assertEquals(1, sectionForm.getDepthToParent());
        List<Concept> sectionFormFields = sectionForm.getFields();
        assertEquals(2, sectionFormFields.size());
        assertEquals(obsConceptName1, sectionFormFields.get(0).getName());
        assertEquals(obsConceptName2, sectionFormFields.get(1).getName());
        assertEquals(1, sectionForm.getChildren().size());
        BahmniForm mostInnerSectionForm = sectionForm.getChildren().get(0);
        assertEquals("Section11", mostInnerSectionForm.getFormName().getName());
        assertEquals(3, mostInnerSectionForm.getDepthToParent());
        assertEquals(1, mostInnerSectionForm.getFields().size());
        assertEquals(obsConceptName3, mostInnerSectionForm.getFields().get(0).getName());
    }

    @Test
    public void shouldGetTheBahmniFormWithChildBahmniFormWhenFormHasObsControlWithMultiSelect() {
        String obsConceptName1 = "ObsConcept1";
        String obsConceptName2 = "ObsConcept2";
        Control obsControl1 = new ControlBuilder()
                .withPropertyMultiSelect(true)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsControl2)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(1, bahmniForm.getFields().size());
        assertFalse(bahmniForm.isMultiSelect());
        assertEquals(obsConceptName2, bahmniForm.getFields().get(0).getName());
        List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(1, bahmniFormChildren.size());
        BahmniForm multiSelectForm = bahmniFormChildren.get(0);
        assertTrue(multiSelectForm.isMultiSelect());
        assertEquals(obsConceptName1, multiSelectForm.getFormName().getName());
        assertEquals(1, multiSelectForm.getFields().size());
        assertEquals(0, multiSelectForm.getChildren().size());
    }

    @Test
    public void shouldNotAddTextFieldsInTheFormWhenIgnoreAllFreeTextConceptsSetToTrueInTheJob() {
        String obsConceptName1 = "ObsConcept1";
        String obsConceptName2 = "ObsConcept2";
        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_concept_1").build();
        obsControl1.getConcept().setDatatype("Text");
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsControl2)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(jobDefinition.getIgnoreAllFreeTextConcepts()).thenReturn(true);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        List<Concept> fields = bahmniForm.getFields();
        assertEquals(1, fields.size());
        assertEquals(obsConceptName2, fields.get(0).getName());
    }
}
