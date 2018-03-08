package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormTableMetadataGeneratorTest {
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Before
    public void setUp() throws Exception {
        formTableMetadataGenerator = new FormTableMetadataGenerator();
    }

    @Test
    public void shouldCreateMetadataForGivenForm() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(123, "formName", 1));
        form.addField(new Concept(1, "field1", 0));
        form.addField(new Concept(2, "field2", 0));

        formTableMetadataGenerator.addMetadataForForm(form);

        assertEquals(1, formTableMetadataGenerator.getTableDataMapSize());
        assertTrue(formTableMetadataGenerator.hasMetadataFor(form));
        TableData tableData = formTableMetadataGenerator.getTableData(form);
        assertEquals("formname", tableData.getName());
        assertTrue(tableData.getColumns().stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("id_formname", "patient_id", "encounter_id", "field1", "field2")));
    }

    @Test
    public void shouldCreateMetadataForExistingForm() {
        BahmniForm grandParentForm = new BahmniForm();
        grandParentForm.setFormName(new Concept(12, "grand parent form", 1));

        BahmniForm parentForm = new BahmniForm();
        parentForm.setFormName(new Concept(122, "parent name", 1));

        BahmniForm child = new BahmniForm();
        parentForm.addChild(child);
        parentForm.setParent(grandParentForm);
        grandParentForm.addChild(parentForm);

        child.setParent(parentForm);
        child.setFormName(new Concept(123, "formName", 1));
        child.addField(new Concept(1, "field1", 0));
        child.addField(new Concept(2, "field2", 0));


        TableData tableData = new TableData("formname");
        tableData.addColumn(new TableColumn("id_formname", "Integer", true, null));
        tableData.addColumn(new TableColumn("patient_id", "Integer", false, null));
        tableData.addColumn(new TableColumn("encounter_id", "Integer", false, null));
        tableData.addColumn(new TableColumn("field1", "Integer", false, null));

        HashMap<String, TableData> stringTableDataHashMap = new HashMap<>();
        stringTableDataHashMap.put("formname", tableData);


        formTableMetadataGenerator.setTableDataMap(stringTableDataHashMap);

        formTableMetadataGenerator.addMetadataForForm(child);

        assertEquals(1, formTableMetadataGenerator.getTableDataMapSize());
        assertTrue(formTableMetadataGenerator.hasMetadataFor(child));
        TableData expectedTableData = formTableMetadataGenerator.getTableData(child);
        assertEquals("formname", expectedTableData.getName());
        assertTrue(expectedTableData.getColumns().stream().map(TableColumn::getName).collect(Collectors.toList())
                .containsAll(Arrays.asList("id_formname", "patient_id", "encounter_id", "field1", "id_parent_name")));
    }

    @Test
    public void shouldGiveAllTheTablesData() {
        BahmniForm form = new BahmniForm();
        form.setFormName(new Concept(123, "formName", 1));
        form.addField(new Concept(1, "field1", 0));
        form.addField(new Concept(2, "field2", 0));

        formTableMetadataGenerator.addMetadataForForm(form);

        List<TableData> tables = formTableMetadataGenerator.getTableDataList();
        assertEquals(1, tables.size());
    }

    @Test
    public void shouldGiveProcessedName() {
        assertEquals("temp_ab_c", FormTableMetadataGenerator.getProcessedName("  Temp  ab c "));
    }
}