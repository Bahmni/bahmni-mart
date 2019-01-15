package org.bahmni.mart.form2.uitl;

import org.bahmni.mart.form2.model.Control;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.junit.Assert;
import org.junit.Test;

public class Form2JsonMetadataReaderTest {
    private static final String FORM_2_METADATA_JSON_DIRECTORY = System.getProperty("user.dir") +
            "/src/test/resources/form2MetadataJson/";

    @Test
    public void shouldReturnTheFormName() {
        Form2JsonMetadata form2JsonMetadata = new Form2MetadataReader()
                .read(FORM_2_METADATA_JSON_DIRECTORY + "EmptyForm_1.json");

        Assert.assertEquals(form2JsonMetadata.getName(), "EmptyForm");
    }

    @Test
    public void shouldReturnEmptyConceptWhenControlDoesNotContainAnyConcept() {
        Form2JsonMetadata form2JsonMetadata = new Form2MetadataReader()
                .read(FORM_2_METADATA_JSON_DIRECTORY + "EmptyForm_1.json");

        Assert.assertEquals(form2JsonMetadata.getName(), "EmptyForm");
        Assert.assertNull(form2JsonMetadata.getControls().get(0).getConcept());
    }

    @Test
    public void shouldReturnConceptWhenControlContainConcept() {
        Form2JsonMetadata form2JsonMetadata = new Form2MetadataReader()
                .read(FORM_2_METADATA_JSON_DIRECTORY + "ComplextForm_2.json");

        Assert.assertEquals(form2JsonMetadata.getName(), "ComplextForm");
        Control childControl = form2JsonMetadata.getControls().get(0);
        Assert.assertNotNull(childControl.getConcept());
        Assert.assertEquals(childControl.getConcept().getName(), "Weight");
    }

    @Test
    public void shouldSetAddMoreTrueInChildControlWhenPropertyIsTrue() {
        Form2JsonMetadata form2JsonMetadata = new Form2MetadataReader()
                .read(FORM_2_METADATA_JSON_DIRECTORY + "ComplextForm_2.json");

        Control childControl = form2JsonMetadata.getControls().get(1);
        Assert.assertTrue(childControl.getProperties().isAddMore());

    }

    @Test
    public void shouldReturnTableLabelsAsControlWithoutConcept() {
        Form2JsonMetadata form2JsonMetadata = new Form2MetadataReader()
                .read(FORM_2_METADATA_JSON_DIRECTORY + "ComplextForm_2.json");

        Control childControl = form2JsonMetadata.getControls().get(3);
        Assert.assertNull(childControl.getConcept());

    }


}
