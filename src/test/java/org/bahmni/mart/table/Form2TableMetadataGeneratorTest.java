package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Form2TableMetadataGeneratorTest {
    private Form2TableMetadataGenerator form2TableMetadataGenerator;
    @Before
    public void setUp() throws Exception {
        form2TableMetadataGenerator = new Form2TableMetadataGenerator();
    }

    @Test
    public void shouldCreateMetadataForGivenForm() {
        BahmniForm form = new BahmniForm();
        final String formName = "FormName";
        form.setFormName(createConcept(formName));
        form.addField(createConcept("field1"));
        form.addField(createConcept("field2"));
        form2TableMetadataGenerator.addMetadataForForm(form);

        assertEquals(1, form2TableMetadataGenerator.getTableDataMapSize());
        assertTrue(form2TableMetadataGenerator.hasMetadataFor(form));
        TableData tableData = form2TableMetadataGenerator.getTableData(form);
        assertEquals(formName.toLowerCase(), tableData.getName());
        assertTrue(tableData.getColumns().stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("patient_id", "encounter_id", "obs_datetime", "date_created",
                        "date_modified", "location_id", "location_name", "program_id", "program_name", "field1",
                        "field2","form_field_path")));
    }

    @Test
    public void shouldCreateMetadataForChildFormsContainingForeignKey() {
        BahmniForm grandParentForm = new BahmniForm();
        grandParentForm.setFormName(createConcept("grand parent form"));

        BahmniForm parentForm = new BahmniForm();
        parentForm.setFormName(createConcept("parent name"));

        BahmniForm child = new BahmniForm();
        parentForm.addChild(child);
        parentForm.setParent(grandParentForm);
        grandParentForm.addChild(parentForm);

        child.setParent(parentForm);
        child.setFormName(createConcept("formName"));
        child.addField(createConcept( "field1"));
        child.addField(createConcept( "field2"));
        form2TableMetadataGenerator.addMetadataForForm(child);

        assertEquals(1, form2TableMetadataGenerator.getTableDataMapSize());
        assertTrue(form2TableMetadataGenerator.hasMetadataFor(child));
        TableData expectedTableData = form2TableMetadataGenerator.getTableData(child);
        assertEquals("formname", expectedTableData.getName());
        assertTrue(expectedTableData.getColumns().stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("form_field_path", "form_field_path_parent_name", "patient_id", "encounter_id", "field1")));
        final List<TableColumn> foreignTableColumns = expectedTableData.getColumns().stream().filter(tableColumn -> tableColumn.getReference() != null).collect(Collectors.toList());
        Assert.assertEquals(foreignTableColumns.size(),2);
        Assert.assertEquals(foreignTableColumns.get(0).getName(), "form_field_path_parent_name");
        Assert.assertEquals(foreignTableColumns.get(1).getName(), "encounter_id");
    }

    @Test
    public void shouldNotHaveForeignKeyReferenceForEncounterIdWhenParentIsNull() {
        BahmniForm form = new BahmniForm();
        form.setFormName(createConcept("formName"));
        form.addField(createConcept( "field1"));
        form.addField(createConcept( "field2"));

        List<TableColumn> expectedTableData = form2TableMetadataGenerator.getColumns(form);
        assertTrue(expectedTableData.stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("form_field_path",  "patient_id", "encounter_id", "field1")));
        final List<TableColumn> foreignTableColumns = expectedTableData.stream().filter(tableColumn -> tableColumn.getReference() != null).collect(Collectors.toList());
        Assert.assertEquals(foreignTableColumns.size(),0);

    }

    private Concept createConcept(String conceptName){
        Concept concept = new Concept();
        concept.setName(conceptName);
        concept.setDataType("Text");
        return concept;
    }

}
