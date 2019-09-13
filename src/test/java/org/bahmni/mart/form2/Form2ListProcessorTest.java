package org.bahmni.mart.form2;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.model.ConceptAnswer;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.bahmni.mart.form2.service.FormService;
import org.bahmni.mart.form2.translations.model.Form2Translation;
import org.bahmni.mart.form2.translations.util.Form2TranslationsReader;
import org.bahmni.mart.form2.uitl.Form2MetadataReader;
import org.bahmni.mart.helper.IgnoreColumnsConfigHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String FORM_PATH = "/ComplexForm_1.json";
    private static final String COMPLEX_FORM = "ComplexForm";
    Map<String, Integer> formNamesWithLatestVersionNumber;
    private Form2ListProcessor form2ListProcessor;
    private Map<String, String> allForms = new HashMap<String, String>();
    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private IgnoreColumnsConfigHelper ignoreColumnsConfigHelper;

    @Mock
    private Form2MetadataReader form2MetadataReader;

    @Mock
    private FormService formService;

    @Mock
    private Form2TranslationsReader form2TranslationsReader;

    @Mock
    private Form2Translation form2Translation;

    @Before
    public void setUp() throws Exception {
        formNamesWithLatestVersionNumber = new HashMap<>();
        formNamesWithLatestVersionNumber.put(COMPLEX_FORM, 1);
        when(formService.getFormNamesWithLatestVersionNumber()).thenReturn(formNamesWithLatestVersionNumber);
        when(form2TranslationsReader.read(COMPLEX_FORM, 1, "en")).thenReturn(form2Translation);
        form2ListProcessor = new Form2ListProcessor(formService, form2TranslationsReader);
        allForms.put(COMPLEX_FORM, FORM_PATH);
        setValuesForMemberFields(form2ListProcessor, "form2MetadataReader", form2MetadataReader);
        setValuesForMemberFields(form2ListProcessor, "ignoreColumnsConfigHelper", ignoreColumnsConfigHelper);
        when(ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition)).thenReturn(new HashSet<>());
        mockStatic(JobDefinitionUtil.class);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(true);
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldsWhenFormHasOnlyObsControl() throws Exception {
        final String obsConceptName = "ObsConcept";
        final String translationKey = "OBS_CONCEPT_1";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, translationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(singletonList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        when(form2TranslationsReader.getTranslation(form2Translation, translationKey)).thenReturn(obsConceptName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertEquals(0, bahmniForm.getChildren().size());
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(1, bahmniFormFields.size());
        assertEquals(obsConceptName, bahmniFormFields.get(0).getName());
    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenWhenFormHasObsControlWithAddMore() {
        final String obsConceptName = "ObsConcept";
        final String translationKey = "OBS_CONCEPT_1";
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, translationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(singletonList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, translationKey)).thenReturn(obsConceptName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertEquals(0, bahmniForm.getFields().size());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(1, bahmniFormChildren.size());
        assertEquals(obsConceptName, bahmniFormChildren.get(0).getFormName().getName());
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldAsObsConceptWhenFormHasObsInsideSection() {
        final String obsConceptName = "ObsConcept";
        final String conceptTranslationKey = "OBS_CONCEPT_1";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";

        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, conceptTranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(singletonList(obsControl))
                .withType("section")
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, conceptTranslationKey))
                .thenReturn(obsConceptName);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey)).thenReturn(sectionName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(1, bahmniFormFields.size());
        assertEquals(obsConceptName, bahmniFormFields.get(0).getName());
    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenAsObsConceptWhenFormHasObsWithAddMoreInsideSection() {
        final String obsConceptName = "ObsConcept";
        final String conceptTranslationKey = "OBS_CONCEPT_1";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";

        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, conceptTranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(singletonList(obsControl))
                .withType("section")
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, conceptTranslationKey))
                .thenReturn(obsConceptName);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey)).thenReturn(sectionName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(0, bahmniForm.getFields().size());
        assertEquals(1, bahmniFormChildren.size());
        assertEquals(obsConceptName, bahmniFormChildren.get(0).getFormName().getName());
    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWithObsConceptAsChildWhenSectionAddMoreContainsObsAddMore() {
        final String obsConceptName = "ObsConcept";
        final String conceptTranslationKey = "OBS_CONCEPT_1";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";

        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, conceptTranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(singletonList(obsControl))
                .withType("section")
                .withPropertyAddMore(true)
                .build();

        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, conceptTranslationKey))
                .thenReturn(obsConceptName);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey)).thenReturn(sectionName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(0, bahmniForm.getFields().size());
        assertEquals(1, bahmniFormChildren.size());
        assertEquals("ComplexForm Section", bahmniFormChildren.get(0).getFormName().getName());
        assertEquals(0, bahmniFormChildren.get(0).getFields().size());
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        assertEquals(1, sectionChildren.size());
        assertEquals(obsConceptName, sectionChildren.get(0).getFormName().getName());
        assertEquals(1, sectionChildren.get(0).getFields().size());
        assertEquals(obsConceptName, sectionChildren.get(0).getFields().get(0).getName());
    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWith2ObsConceptAsChildrenWhenSectionAddMoreContains2ObsAddMore() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String concept1TranslationKey = "OBS_CONCEPT_1";
        final String concept2TranslationKey = "OBS_CONCEPT_2";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, concept1TranslationKey).build();

        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, concept2TranslationKey).build();

        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withType("section")
                .withPropertyAddMore(true)
                .build();

        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, concept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, concept2TranslationKey))
                .thenReturn(obsConceptName2);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey))
                .thenReturn(sectionName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertNull(bahmniForm.getRootForm());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(0, bahmniForm.getFields().size());
        assertEquals(1, bahmniFormChildren.size());
        assertEquals("ComplexForm Section", bahmniFormChildren.get(0).getFormName().getName());
        assertEquals(0, bahmniFormChildren.get(0).getFields().size());
        assertEquals(bahmniFormChildren.get(0).getRootForm(), bahmniForm);
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        assertEquals(2, sectionChildren.size());
        assertEquals(obsConceptName1, sectionChildren.get(0).getFormName().getName());
        assertEquals(obsConceptName2, sectionChildren.get(1).getFormName().getName());
        assertEquals(2, sectionChildren.get(0).getDepthToParent());
        assertEquals(2, sectionChildren.get(1).getDepthToParent());
        assertEquals(bahmniForm, sectionChildren.get(0).getRootForm());
        assertEquals(bahmniForm, sectionChildren.get(1).getRootForm());
    }

    @Test
    public void shouldReturnBahmniFormWith2ObsConceptAsChildrenWhenSectionContains2ObsAddMore() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String concept1TranslationKey = "OBS_CONCEPT_1";
        final String concept2TranslationKey = "OBS_CONCEPT_2";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, concept1TranslationKey).build();

        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, concept2TranslationKey).build();

        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withType("section")
                .withPropertyAddMore(false)
                .build();

        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, concept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, concept2TranslationKey))
                .thenReturn(obsConceptName2);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey))
                .thenReturn(sectionName);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(0, bahmniForm.getFields().size());
        assertEquals(2, bahmniFormChildren.size());
        assertEquals(obsConceptName1, bahmniFormChildren.get(0).getFormName().getName());
        assertEquals(obsConceptName2, bahmniFormChildren.get(1).getFormName().getName());
        assertEquals(1, bahmniFormChildren.get(0).getDepthToParent());
        assertEquals(1, bahmniFormChildren.get(1).getDepthToParent());
    }

    @Test
    public void shouldReturnBahmniFormWithTableObsWhenFormContainsATable() {
        final String obsConceptName1 = "ObsConcept1";
        final String translationKey = "OBS_CONCEPT_1";

        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, translationKey).build();

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control emptyControl1 = new ControlBuilder().build();
        Control emptyControl2 = new ControlBuilder().build();
        Control tableControl = new ControlBuilder()
                .withControls(Arrays.asList(emptyControl1, emptyControl2, obsControl))
                .withPropertyAddMore(false)
                .withLabel("Table", "TABLE_1")
                .build();

        form2JsonMetadata.setControls(singletonList(tableControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(1, bahmniForm.getFields().size());
        assertEquals(0, bahmniFormChildren.size());
    }

    @Test
    public void shouldFilterOutFormsWithDuplicateConcepts() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String concept1TranslationKey = "OBS_CONCEPT_1";
        final String concept2TranslationKey = "OBS_CONCEPT_2";
        final String section1TranslationKey = "SECTION_1";
        final String section2TranslationKey = "SECTION_2";
        String sectionType = "section";
        String section = "Section";

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, concept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, concept2TranslationKey).build();
        Control sectionControl1 = new ControlBuilder()
                .withLabel(section, section1TranslationKey)
                .withControls(Arrays.asList(obsControl1, obsControl1, obsControl2))
                .withType(sectionType)
                .withPropertyAddMore(false)
                .build();
        Control sectionControl2 = new ControlBuilder()
                .withLabel(section, section2TranslationKey)
                .withControls(singletonList(obsControl2))
                .withType(sectionType)
                .withPropertyAddMore(false)
                .build();

        form2JsonMetadata.setControls(Arrays.asList(sectionControl1, sectionControl2));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, concept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, concept2TranslationKey))
                .thenReturn(obsConceptName2);
        when(form2TranslationsReader.getTranslation(form2Translation, section1TranslationKey))
                .thenReturn(section);
        when(form2TranslationsReader.getTranslation(form2Translation, section2TranslationKey))
                .thenReturn(section);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(0, allForms.size());
    }

    @Test
    public void shouldFilterOutConceptsGivenInIgnoreConceptNames() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String concept1TranslationKey = "OBS_CONCEPT_1";
        final String concept2TranslationKey = "OBS_CONCEPT_2";
        final String section1TranslationKey = "SECTION_1";
        String sectionType = "section";
        String section = "Section";

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, concept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, concept2TranslationKey).build();
        Concept concept2 = new Concept(2, obsConceptName2, 0);
        Control sectionControl1 = new ControlBuilder()
                .withControls(Arrays.asList(obsControl1, obsControl2))
                .withType(sectionType)
                .withPropertyAddMore(false)
                .withLabel(section, section1TranslationKey)
                .build();
        HashSet<Concept> ignoredConcepts = new HashSet<>();
        ignoredConcepts.add(concept2);
        when(ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition)).thenReturn(ignoredConcepts);
        form2JsonMetadata.setControls(singletonList(sectionControl1));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, concept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, concept2TranslationKey))
                .thenReturn(obsConceptName2);
        when(form2TranslationsReader.getTranslation(form2Translation, section1TranslationKey))
                .thenReturn(section);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final List<Concept> fields = allForms.get(0).getFields();
        assertEquals(1, fields.size());
        assertEquals(obsConceptName1, fields.get(0).getName());
    }

    @Test
    public void shouldNotAddChildrenForAddMoreAndMultiSelectWhenEnableAddMoreAndMultiSelectIsFalse() {
        String multiSelectConceptName = "multiSelectObsConcept";
        String multiSelectConceptTranslationKey = "MULTI_SELECT_OBS_CONCEPT";
        Control multiSelectObsControl = new ControlBuilder()
                .withPropertyMultiSelect(true)
                .withLabel(multiSelectConceptName, multiSelectConceptTranslationKey)
                .withConcept(multiSelectConceptName, "obs_concept_1").build();

        String addMoreConceptName = "addMoreObsConcept";
        String addMoreConceptTranslationKey = "ADD_MORE_OBS_CONCEPT";
        Control addMoreObsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withLabel(addMoreConceptName, addMoreConceptTranslationKey)
                .withConcept(addMoreConceptName, "obs_concept_2").build();

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(Arrays.asList(multiSelectObsControl, addMoreObsControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition)).thenReturn(false);
        when(form2TranslationsReader.getTranslation(form2Translation, multiSelectConceptTranslationKey))
                .thenReturn(multiSelectConceptName);
        when(form2TranslationsReader.getTranslation(form2Translation, addMoreConceptTranslationKey))
                .thenReturn(addMoreConceptName);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        List<Concept> fields = allForms.get(0).getFields();
        assertEquals(0, allForms.get(0).getChildren().size());
        assertEquals(2, fields.size());
        assertEquals(multiSelectConceptName, fields.get(0).getName());
        assertEquals(addMoreConceptName, fields.get(1).getName());
    }

    @Test
    public void shouldReturnBahmniFormHavingSectionWith2NestedSectionsAndTheFirstInnerSectionIsAddMoreWithObs() {
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        final String obsConceptName3 = "ObsConcept3";
        final String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        final String obsConcept2TranslationKey = "OBS_CONCEPT_2";
        final String obsConcept3TranslationKey = "OBS_CONCEPT_3";

        String sectionType = "section";

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withLabel(obsConceptName2, obsConcept2TranslationKey)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        Control obsControl3 = new ControlBuilder()
                .withConcept(obsConceptName3, "obs_concept_3")
                .withLabel(obsConceptName3, obsConcept3TranslationKey).build();

        String innerMostSection = "Section11";
        String innerMostSectionTranslationKey = "SECTION_11";
        Control mostInnerSectionControl = new ControlBuilder()
                .withLabel(innerMostSection, innerMostSectionTranslationKey)
                .withControls(Collections.singletonList(obsControl3))
                .withType(sectionType)
                .withPropertyAddMore(true)
                .build();
        String innerSection = "Section1";
        String innerSectionTranslationKey = "SECTION_1";
        Control innerSectionControl = new ControlBuilder()
                .withLabel(innerSection, innerSectionTranslationKey)
                .withControls(Arrays.asList(obsControl2, mostInnerSectionControl))
                .withType(sectionType)
                .withPropertyAddMore(false)
                .build();
        String section = "Section";
        String sectionTranslationKey = "SECTION";
        Control sectionControl = new ControlBuilder()
                .withLabel(section, sectionTranslationKey)
                .withControls(Arrays.asList(obsControl1, innerSectionControl))
                .withType(sectionType)
                .withPropertyAddMore(true)
                .build();

        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept3TranslationKey))
                .thenReturn(obsConceptName3);
        when(form2TranslationsReader.getTranslation(form2Translation, innerMostSectionTranslationKey))
                .thenReturn(innerMostSection);
        when(form2TranslationsReader.getTranslation(form2Translation, innerSectionTranslationKey))
                .thenReturn(innerSection);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey))
                .thenReturn(section);


        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(allForms.size(), 1);
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertNull(bahmniForm.getRootForm());
        assertEquals(0, bahmniForm.getFields().size());
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        assertEquals(1, bahmniFormChildren.size());
        BahmniForm sectionForm = bahmniFormChildren.get(0);
        assertEquals("ComplexForm Section", sectionForm.getFormName().getName());
        assertEquals(1, sectionForm.getDepthToParent());
        List<Concept> sectionFormFields = sectionForm.getFields();
        assertEquals(2, sectionFormFields.size());
        assertEquals(obsConceptName1, sectionFormFields.get(0).getName());
        assertEquals(obsConceptName2, sectionFormFields.get(1).getName());
        assertEquals(1, sectionForm.getChildren().size());
        BahmniForm mostInnerSectionForm = sectionForm.getChildren().get(0);
        assertEquals("ComplexForm Section11", mostInnerSectionForm.getFormName().getName());
        assertEquals(3, mostInnerSectionForm.getDepthToParent());
        assertEquals(1, mostInnerSectionForm.getFields().size());
        assertEquals(obsConceptName3, mostInnerSectionForm.getFields().get(0).getName());
    }

    @Test
    public void shouldGetTheBahmniFormWithChildBahmniFormWhenFormHasObsControlWithMultiSelect() {
        String obsConceptName1 = "ObsConcept1";
        String obsConceptName2 = "ObsConcept2";
        String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        String obsConcept2TranslationKey = "OBS_CONCEPT_2";
        Control obsControl1 = new ControlBuilder()
                .withPropertyMultiSelect(true)
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, obsConcept2TranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsControl2)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2);

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
    public void shouldNotAddTheConceptsInIgnoreConcepts() {
        String obsConceptName1 = "ObsConcept1";
        String obsConceptName2 = "ObsConcept2";
        String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        String obsConcept2TranslationKey = "OBS_CONCEPT_2";

        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_concept_1")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        obsControl1.getConcept().setDatatype("Text");
        Concept concept1 = new Concept(1, obsConceptName1, 0);
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_concept_2")
                .withLabel(obsConceptName2, obsConcept2TranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsControl2)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition))
                .thenReturn(new HashSet<>(Arrays.asList(concept1)));
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(obsConceptName1);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        List<Concept> fields = bahmniForm.getFields();
        assertEquals(1, fields.size());
        assertEquals(obsConceptName2, fields.get(0).getName());
    }

    @Test
    public void shouldCreateMapWithTheTranslatedValuesInBahmniFormWithObsAddMoreAndObsGroup() {
        String obsConceptName1 = "ObsConcept1";
        String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        String obsConceptName2 = "ObsConcept2";
        String obsConcept2TranslationKey = "OBS_CONCEPT_2";
        Control obsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_control_1_uuid")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_control_2_uuid")
                .withLabel(obsConceptName2, obsConcept2TranslationKey).build();
        Control obsGroupControl1 = new ControlBuilder()
                .withLabel("obsGroupControl", "OBS_GROUP_CONTROL_1")
                .withControls(Collections.singletonList(obsControl2)).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsGroupControl1)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        String obsConceptName1InGivenLocale = "ObsConcept1InGivenLocale";
        String obsConceptName2InGivenLocale = "ObsConcept2InGivenLocale";
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(obsConceptName1InGivenLocale);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2InGivenLocale);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(1, bahmniForm.getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(1, bahmniForm.getChildren().get(0).getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(obsConceptName1InGivenLocale, bahmniForm.getChildren().get(0)
                .getFieldNameAndFullySpecifiedNameMap().get(obsConceptName1));
        assertEquals(obsConceptName2InGivenLocale,
                bahmniForm.getFieldNameAndFullySpecifiedNameMap().get(obsConceptName2));
    }

    @Test
    public void shouldCreateMapWithTheTranslatedValuesInBahmniFormWithSectionAndObsGroupAddMore() {
        String obsConceptName1 = "ObsConcept1";
        String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        String obsConceptName2 = "ObsConcept2";
        String obsConcept2TranslationKey = "OBS_CONCEPT_2";
        String sectionName = "Section";
        String sectionTranslationKey = "SECTION_1";

        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_control_1_uuid")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_control_2_uuid")
                .withLabel(obsConceptName2, obsConcept2TranslationKey).build();
        Control obsGroupControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withLabel("obsGroupControl", "OBS_GROUP_CONTROL_1")
                .withControls(Collections.singletonList(obsControl2)).build();
        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(singletonList(obsControl1))
                .withType("section")
                .withPropertyAddMore(false)
                .build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(sectionControl, obsGroupControl1)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        String obsConceptName1InGivenLocale = "ObsConcept1InGivenLocale";
        String obsConceptName2InGivenLocale = "ObsConcept2InGivenLocale";
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(obsConceptName1InGivenLocale);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2InGivenLocale);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(1, bahmniForm.getChildren().get(0).getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(1, bahmniForm.getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(obsConceptName1InGivenLocale, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptName1));
        assertEquals(obsConceptName2InGivenLocale, bahmniForm.getChildren().get(0)
                .getFieldNameAndFullySpecifiedNameMap().get(obsConceptName2));
    }

    @Test
    public void shouldReturnBahmniFormMapWithTranslationsOfCodedConceptAnswer() {
        String obsConceptName = "ObsConcept";
        String obsConceptAnswerName = "ObsConceptAnswer";
        String obsConceptTranslationKey = "OBS_CONCEPT";
        String obsConceptAnswerTranslationKey = "OBS_CONCEPT_ANSWER";

        ConceptAnswer conceptAnswer = new ConceptAnswer();
        conceptAnswer.setDisplayString(obsConceptAnswerName);
        conceptAnswer.setTranslationKey(obsConceptAnswerTranslationKey);

        org.bahmni.mart.form2.model.Concept concept = new org.bahmni.mart.form2.model.Concept();
        concept.setName(obsConceptName);
        concept.setDatatype("Coded");
        concept.setAnswers(Collections.singletonList(conceptAnswer));

        Control obsControl = new ControlBuilder()
                .withConcept(concept)
                .withLabel(obsConceptName, obsConceptTranslationKey).build();

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        String obsConceptNameInGivenLocale = "ObsConcept1InGivenLocale";
        when(form2TranslationsReader.getTranslation(form2Translation, obsConceptTranslationKey))
                .thenReturn(obsConceptNameInGivenLocale);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConceptAnswerTranslationKey))
                .thenReturn(obsConceptAnswerName);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        verify(form2TranslationsReader, times(2)).getTranslation(any(), any());
        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(2, bahmniForm.getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(obsConceptNameInGivenLocale, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptName));
        assertEquals(obsConceptAnswerName, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptAnswerName));
    }

    @Test
    public void shouldGetConceptNameInDefaultLanguageWhenTranslationForConceptIsNotPresent() {
        String obsConceptName1 = "ObsConcept1";
        String obsConcept1TranslationKey = "OBS_CONCEPT_1";
        String obsConceptName2 = "ObsConcept2";
        String obsConcept2TranslationKey = "OBS_CONCEPT_2";
        Control obsControl1 = new ControlBuilder()
                .withConcept(obsConceptName1, "obs_control_1_uuid")
                .withLabel(obsConceptName1, obsConcept1TranslationKey).build();
        Control obsControl2 = new ControlBuilder()
                .withConcept(obsConceptName2, "obs_control_2_uuid")
                .withLabel(obsConceptName2, obsConcept2TranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl1, obsControl2)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        String obsConceptName2InGivenLocale = "ObsConcept2InGivenLocale";
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept1TranslationKey))
                .thenReturn(null);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConcept2TranslationKey))
                .thenReturn(obsConceptName2InGivenLocale);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(2, bahmniForm.getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals((new Concept(null, obsConceptName1, null)), bahmniForm.getFields().get(0));
        assertEquals((new Concept(null, obsConceptName2InGivenLocale, null)), bahmniForm.getFields().get(1));
        assertEquals(obsConceptName1, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptName1));
        assertEquals(obsConceptName2InGivenLocale, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptName2));
    }

    @Test
    public void shouldReturnBahmniFormMapWithDefaultConceptAnswerNamesWhenTranslationIsNotAvailable() {
        String obsConceptName = "ObsConcept";
        String obsConceptAnswerName1 = "ObsConceptAnswer1";
        String obsConceptAnswerName2 = "ObsConceptAnswer2";
        String obsConceptTranslationKey = "OBS_CONCEPT";
        String obsConceptAnswer1TranslationKey = "OBS_CONCEPT_ANSWER_1";
        String obsConceptAnswer2TranslationKey = "OBS_CONCEPT_ANSWER_2";

        ConceptAnswer conceptAnswer1 = new ConceptAnswer();
        conceptAnswer1.setDisplayString(obsConceptAnswerName1);
        conceptAnswer1.setTranslationKey(obsConceptAnswer1TranslationKey);

        ConceptAnswer conceptAnswer2 = new ConceptAnswer();
        conceptAnswer2.setDisplayString(obsConceptAnswerName2);
        conceptAnswer2.setTranslationKey(obsConceptAnswer2TranslationKey);

        org.bahmni.mart.form2.model.Concept concept = new org.bahmni.mart.form2.model.Concept();
        concept.setName(obsConceptName);
        concept.setDatatype("Coded");
        concept.setAnswers(Arrays.asList(conceptAnswer1, conceptAnswer2));

        Control obsControl = new ControlBuilder()
                .withConcept(concept)
                .withLabel(obsConceptName, obsConceptTranslationKey).build();

        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(obsControl)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        String obsConceptNameInGivenLocale = "ObsConcept1InGivenLocale";
        String obsConceptAnswerName1InGivenLocale = "ObsConcept1InGivenLocale";
        when(form2TranslationsReader.getTranslation(form2Translation, obsConceptTranslationKey))
                .thenReturn(obsConceptNameInGivenLocale);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConceptAnswer1TranslationKey))
                .thenReturn(obsConceptAnswerName1InGivenLocale);
        when(form2TranslationsReader.getTranslation(form2Translation, obsConceptAnswer2TranslationKey))
                .thenReturn(null);

        List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        verify(form2TranslationsReader, times(3)).getTranslation(any(), any());
        assertEquals(1, allForms.size());
        BahmniForm bahmniForm = allForms.get(0);
        assertEquals(3, bahmniForm.getFieldNameAndFullySpecifiedNameMap().size());
        assertEquals(obsConceptNameInGivenLocale, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptName));
        assertEquals(obsConceptAnswerName1InGivenLocale, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptAnswerName1));
        assertEquals(obsConceptAnswerName2, bahmniForm.getFieldNameAndFullySpecifiedNameMap()
                .get(obsConceptAnswerName2));
    }

    @Test
    public void shouldReturnBahmniFormMapWithDefaultSectionNameWhenTranslationIsNotAvailable() {
        final String obsConceptName = "ObsConcept";
        final String conceptTranslationKey = "OBS_CONCEPT_1";
        String sectionName = "Section";
        final String sectionTranslationKey = "SECTION_1";

        Control obsControl = new ControlBuilder()
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, conceptTranslationKey).build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        Control sectionControl = new ControlBuilder()
                .withLabel(sectionName, sectionTranslationKey)
                .withControls(singletonList(obsControl))
                .withType("section")
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(singletonList(sectionControl));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, conceptTranslationKey))
                .thenReturn(obsConceptName);
        when(form2TranslationsReader.getTranslation(form2Translation, sectionTranslationKey)).thenReturn(null);

        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(0, bahmniFormFields.size());
        assertEquals(obsConceptName, bahmniForm.getChildren().get(0).getFields().get(0).getName());
        assertEquals(COMPLEX_FORM + " " + sectionName, bahmniForm.getChildren().get(0).getFormName().getName());
    }

    @Test
    public void shouldAvoidTextConceptsWhenIgnoreFreeTextPropertyIsTrueInNonDefaultLocale() {
        final String obsConceptName = "ObsConcept";
        final String translationKey = "OBS_CONCEPT_1";
        Map<String, String> conceptTranslations = new HashMap<>();
        conceptTranslations.put(translationKey, "FrenchConcept");
        Form2Translation form2TranslationObj = new Form2Translation();
        form2TranslationObj.setConcepts(conceptTranslations);
        form2TranslationObj.setLocale("fr");
        form2TranslationObj.setFormName(COMPLEX_FORM);
        form2TranslationObj.setVersion("1");
        Control obsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1")
                .withLabel(obsConceptName, translationKey).build();
        Concept ignoreConcept = new Concept(2, obsConceptName, 0);
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(singletonList(obsControl)));

        when(jobDefinition.getLocale()).thenReturn("fr");
        when(form2TranslationsReader.getTranslation(any(), any())).thenReturn("FrenchConcept");
        when(ignoreColumnsConfigHelper.getIgnoreConceptsForJob(jobDefinition))
                .thenReturn(new HashSet<>(Collections.singletonList(ignoreConcept)));
        when(form2MetadataReader.read(FORM_PATH)).thenReturn(form2JsonMetadata);
        when(form2TranslationsReader.getTranslation(form2Translation, translationKey)).thenReturn(obsConceptName);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);

        assertEquals(1, allForms.size());
        final BahmniForm bahmniForm = allForms.get(0);
        assertEquals(COMPLEX_FORM, bahmniForm.getFormName().getName());
        assertEquals(0, bahmniForm.getChildren().size());
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        assertEquals(0, bahmniFormFields.size());
    }
}
