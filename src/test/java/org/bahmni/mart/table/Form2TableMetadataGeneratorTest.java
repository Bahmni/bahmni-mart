package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
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
                .containsAll(Arrays.asList("patient_id", "visit_id", "encounter_id", "obs_datetime", "date_created",
                        "date_modified", "location_id", "location_name", "program_id", "program_name",
                        "patient_program_id", "field1", "field2", "form_field_path")));
    }

    @Test
    public void shouldCreateMetadataForChildFormsContainingReferenceKey() {
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
        child.addField(createConcept("field1"));
        child.addField(createConcept("field2"));

        form2TableMetadataGenerator.addMetadataForForm(child);

        assertEquals(1, form2TableMetadataGenerator.getTableDataMapSize());
        assertTrue(form2TableMetadataGenerator.hasMetadataFor(child));
        TableData actualTableData = form2TableMetadataGenerator.getTableData(child);
        assertEquals("formname", actualTableData.getName());
        List<String> expectedColumns = Arrays.asList("form_field_path", "reference_form_field_path",
                "patient_id", "encounter_id", "visit_id", "obs_datetime", "date_created",
                "date_modified", "location_id", "location_name", "program_id", "program_name",
                "patient_program_id", "field1");
        List<String> actualColumns = actualTableData.getColumns().stream()
                .map(TableColumn::getName).collect(Collectors.toList());
        assertTrue(actualColumns.containsAll(expectedColumns));
    }

    @Test
    public void shouldNotHaveForeignKeyReferenceForEncounterIdWhenParentIsNull() {
        BahmniForm form = new BahmniForm();
        form.setFormName(createConcept("formName"));
        form.addField(createConcept("field1"));
        form.addField(createConcept("field2"));

        List<TableColumn> expectedTableData = form2TableMetadataGenerator.getColumns(form);
        assertTrue(expectedTableData.stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("form_field_path", "patient_id", "encounter_id", "field1")));
        final List<TableColumn> foreignTableColumns = expectedTableData.stream()
                .filter(tableColumn -> nonNull(tableColumn.getReference())).collect(Collectors.toList());
        Assert.assertEquals(foreignTableColumns.size(), 0);

    }

    @Test
    public void shouldReturnTranslatedFormNameAsTableNameAfterUpdatingMetadata() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(123, "Form Two Name", 1));
        form.setTranslatedFormName("Form Two Name French");

        form2TableMetadataGenerator.addMetadataForForm(form);

        TableData tableData = form2TableMetadataGenerator.getTableData(form);
        assertEquals("form_two_name_french", tableData.getName());
    }

    @Test
    public void shouldReturnFormNameAsTableNameWhenTranslatedNameIsNotAvailable() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(123, "Form Two Name", 1));

        form2TableMetadataGenerator.addMetadataForForm(form);

        TableData tableData = form2TableMetadataGenerator.getTableData(form);
        assertEquals("form_two_name", tableData.getName());
    }

    private Concept createConcept(String conceptName) {
        Concept concept = new Concept();
        concept.setName(conceptName);
        concept.setDataType("Text");
        return concept;
    }

}
