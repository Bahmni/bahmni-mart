package org.bahmni.mart.form2;


import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.bahmni.mart.form2.uitl.Form2MetadataReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({JobDefinitionUtil.class})
@RunWith(PowerMockRunner.class)
public class Form2ListProcessorTest {

    private Form2ListProcessor form2ListProcessor;

    Map<String , String> allForms = new HashMap<String, String>();

    private static final String FORM_2_METADATA_JSON_DIRECTORY = System.getProperty("user.dir") +
            "/src/test/resources/form2MetadataJson/";
    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Form2MetadataReader form2MetadataReader;

    private final String formPath = "SomeForm.json";
    private final String complextForm = "ComplextForm";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        form2ListProcessor = new Form2ListProcessor();
        allForms.put(complextForm, formPath);
        setValuesForMemberFields(form2ListProcessor, "form2MetadataReader", form2MetadataReader);
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldsWhenFormHasOnlyObsControl(){
        Control ObsControl;
        final String obsConceptName = "ObsConcept";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(ObsControl)));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        Assert.assertEquals(bahmniForm.getChildren().size(), 0);
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        Assert.assertEquals(bahmniFormFields.size(), 1);
        Assert.assertEquals(bahmniFormFields.get(0).getName(), obsConceptName);
    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenWhenFormHasObsControlWithAddMore(){
        Control ObsControl;
        final String obsConceptName = "ObsConcept";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        form2JsonMetadata.setControls(new ArrayList<>(Arrays.asList(ObsControl)));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        Assert.assertEquals(bahmniForm.getFields().size(), 0);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniFormChildren.size(), 1);
        Assert.assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName);
    }

    @Test
    public void shouldGetTheBahmniFormWithFieldAsObsConceptWhenFormHasObsInsideSection(){
        Control ObsControl, SectionControl;
        final String obsConceptName = "ObsConcept";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        SectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<Concept> bahmniFormFields = bahmniForm.getFields();
        Assert.assertEquals(bahmniFormFields.size(), 1);
        Assert.assertEquals(bahmniFormFields.get(0).getName(), obsConceptName);

    }

    @Test
    public void shouldGetTheBahmniFormWithChildrenAsObsConceptWhenFormHasObsWithAddMoreInsideSection(){
        Control ObsControl, SectionControl;
        final String obsConceptName = "ObsConcept";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        SectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniForm.getFields().size(), 0);
        Assert.assertEquals(bahmniFormChildren.size(), 1);
        Assert.assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName);

    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWithObsConceptAsChildWhenSectionAddMoreContainsObsAddMore(){
        Control ObsControl, SectionControl;
        final String obsConceptName = "ObsConcept";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        SectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl))
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniForm.getFields().size(), 0);
        Assert.assertEquals(bahmniFormChildren.size(), 1);
        Assert.assertEquals(bahmniFormChildren.get(0).getFormName().getName(), "Section");
        Assert.assertEquals(bahmniFormChildren.get(0).getFields().size(),0);
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        Assert.assertEquals(sectionChildren.size(),1);
        Assert.assertEquals(sectionChildren.get(0).getFormName().getName(), obsConceptName);

    }

    @Test
    public void shouldReturnBahmniFormChildAsSectionWith2ObsConceptAsChildrenWhenSectionAddMoreContains2ObsAddMore(){
        Control ObsControl1,ObsControl2, SectionControl;
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        ObsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        ObsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        SectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl1, ObsControl2))
                .withPropertyAddMore(true)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniForm.getFields().size(), 0);
        Assert.assertEquals(bahmniFormChildren.size(), 1);
        Assert.assertEquals(bahmniFormChildren.get(0).getFormName().getName(), "Section");
        Assert.assertEquals(bahmniFormChildren.get(0).getFields().size(),0);
        final List<BahmniForm> sectionChildren = bahmniFormChildren.get(0).getChildren();
        Assert.assertEquals(sectionChildren.size(),2);
        Assert.assertEquals(sectionChildren.get(0).getFormName().getName(), obsConceptName1);
        Assert.assertEquals(sectionChildren.get(1).getFormName().getName(), obsConceptName2);

    }

    @Test
    public void shouldReturnBahmniFormWith2ObsConceptAsChildrenWhenSectionContains2ObsAddMore(){
        Control ObsControl1,ObsControl2, SectionControl;
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        ObsControl1 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        ObsControl2 = new ControlBuilder()
                .withPropertyAddMore(true)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        SectionControl = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl1, ObsControl2))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniForm.getFields().size(), 0);
        Assert.assertEquals(bahmniFormChildren.size(), 2);
        Assert.assertEquals(bahmniFormChildren.get(0).getFormName().getName(), obsConceptName1);
        Assert.assertEquals(bahmniFormChildren.get(1).getFormName().getName(), obsConceptName2);
    }

    @Test
    public void shouldReturnBahmniFormWithTableObsWhenFormContainsATable(){
        Control EmptyControl1, EmptyControl2, TableControl, ObsControl;
        final String obsConceptName1 = "ObsConcept1";
        ObsControl = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        EmptyControl1 = new ControlBuilder().build();
        EmptyControl2 = new ControlBuilder().build();
        TableControl = new ControlBuilder()
                .withControls(Arrays.asList(EmptyControl1,EmptyControl2,ObsControl))
                .withPropertyAddMore(false)
                .withLabel("Table")
                .build();

        form2JsonMetadata.setControls(Arrays.asList(TableControl));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final BahmniForm bahmniForm = allForms.get(0);
        Assert.assertEquals(bahmniForm.getFormName().getName(), complextForm);
        final List<BahmniForm> bahmniFormChildren = bahmniForm.getChildren();
        Assert.assertEquals(bahmniForm.getFields().size(), 1);
        Assert.assertEquals(bahmniFormChildren.size(), 0);

    }

    @Test
    public void shouldFilterOutFormsWithDuplicateConcepts(){
        Control ObsControl1,ObsControl2, SectionControl1, SectionControl2;
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        ObsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        ObsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        SectionControl1 = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl1,ObsControl1, ObsControl2))
                .withPropertyAddMore(false)
                .build();
        SectionControl2 =  new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl2))
                .withPropertyAddMore(false)
                .build();
        form2JsonMetadata.setControls(Arrays.asList(SectionControl1, SectionControl2));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),0);
    }

    @Test
    public void shouldFilterOutConceptsGivenInIgnoreConceptNames(){

        Control ObsControl1,ObsControl2, SectionControl1;
        final String obsConceptName1 = "ObsConcept1";
        final String obsConceptName2 = "ObsConcept2";
        Form2JsonMetadata form2JsonMetadata = new Form2JsonMetadata();
        ObsControl1 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName1, "obs_concept_1").build();
        ObsControl2 = new ControlBuilder()
                .withPropertyAddMore(false)
                .withConcept(obsConceptName2, "obs_concept_2").build();
        SectionControl1 = new ControlBuilder()
                .withLabel("Section")
                .withControls(Arrays.asList(ObsControl1, ObsControl2))
                .withPropertyAddMore(false)
                .build();
        when(jobDefinition.getColumnsToIgnore()).thenReturn(Arrays.asList(obsConceptName2));
        form2JsonMetadata.setControls(Arrays.asList(SectionControl1));
        when(form2MetadataReader.read(formPath)).thenReturn(form2JsonMetadata);
        final List<BahmniForm> allForms = form2ListProcessor.getAllForms(this.allForms, jobDefinition);
        Assert.assertEquals(allForms.size(),1);
        final List<Concept> fields = allForms.get(0).getFields();
        Assert.assertEquals(fields.size(),1);
        Assert.assertEquals(fields.get(0).getName(), obsConceptName1);
    }


}
