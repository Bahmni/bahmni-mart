package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.TableDataGenerator;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormTableMetadataGeneratorTest {
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private TableDataGenerator tableDataGenerator;

    @Before
    public void setUp() throws Exception {
        formTableMetadataGenerator = new FormTableMetadataGenerator();
        setValuesForMemberFields(formTableMetadataGenerator, "tableDataGenerator", tableDataGenerator);
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

    @Test
    public void shouldAddPrefixToGivenName() {
        assertEquals("reg test_name", FormTableMetadataGenerator.addPrefixToName("test_name", "reg"));
    }

    @Test
    public void shouldReturnGivenNameAsItIsIfPrefixIsNull() {
        assertEquals("test_name", FormTableMetadataGenerator.addPrefixToName("test_name", null));
    }

    @Test
    public void shouldReturnGivenNameAsItIsIfPrefixIsEmpty() {
        assertEquals("test_name", FormTableMetadataGenerator.addPrefixToName("test_name", null));
    }

    @Test
    public void shouldNotAddTableDataToMapWhenThereIsNoMetadataChange() {
        BahmniForm form = new BahmniForm();
        String formName = "formName";
        form.setFormName(new Concept(123, formName, 1));
        Concept field1 = new Concept(1, "field,1", 0);
        field1.setDataType("int");
        Concept field2 = new Concept(2, "field2", 0);
        field2.setDataType("text");
        form.addField(field1);
        form.addField(field2);

        TableData existingTableData = new TableData("formname");
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formname","integer",true,null));
        tableColumns.add(new TableColumn("field_1","int",false,null));
        tableColumns.add(new TableColumn("field2","text",false,null));
        tableColumns.add(new TableColumn("patient_id", "integer", false, null));
        tableColumns.add(new TableColumn("encounter_id", "integer", false, null));
        tableColumns.add(new TableColumn("obs_datetime", "text", false, null));
        tableColumns.add(new TableColumn("location_id", "integer", false, null));
        tableColumns.add(new TableColumn("location_name", "text", false, null));
        tableColumns.add(new TableColumn("program_id", "integer", false, null));
        tableColumns.add(new TableColumn("program_name", "text", false, null));
        existingTableData.setColumns(tableColumns);
        when(tableDataGenerator.getTableDataFromMart(formName.toLowerCase(),"SELECT * FROM formname"))
                .thenReturn(existingTableData);

        formTableMetadataGenerator.addMetadataForForm(form);

        assertTrue(formTableMetadataGenerator.getTableDataList().isEmpty());
    }

    @Test
    public void shouldAddTableDataToMapOnlyWhenMetaDataChanged() {
        BahmniForm formOne = new BahmniForm();
        String formOneName = "formOneName";
        formOne.setFormName(new Concept(123, formOneName, 1));
        Concept fieldOneOfOne = new Concept(1, "field1", 0);
        fieldOneOfOne.setDataType("int");
        formOne.addField(fieldOneOfOne);

        BahmniForm formTwo = new BahmniForm();
        String formTwoName = "formTwoName";
        formTwo.setFormName(new Concept(123, formTwoName, 1));
        Concept fieldOneOfTwo = new Concept(1, "field2", 0);
        fieldOneOfTwo.setDataType("int");
        Concept fieldTwoOfTwo = new Concept(2, "field3", 0);
        fieldTwoOfTwo.setDataType("text");
        formTwo.addField(fieldOneOfTwo);
        formTwo.addField(fieldTwoOfTwo);

        TableData expected = new TableData("formtwoname");
        TableColumn tableColumnOne = new TableColumn("field2","int",false,null);
        TableColumn tableColumnTwo = new TableColumn("field3","text",false,null);
        expected.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        TableData existingTableData = new TableData();
        existingTableData.setName(formOneName.toLowerCase());
        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("id_formonename","integer",true,null));
        tableColumns.add(new TableColumn("field1","int",false,null));
        tableColumns.add(new TableColumn("patient_id", "integer", false, null));
        tableColumns.add(new TableColumn("encounter_id", "integer", false, null));
        tableColumns.add(new TableColumn("obs_datetime", "text", false, null));
        tableColumns.add(new TableColumn("location_id", "integer", false, null));
        tableColumns.add(new TableColumn("location_name", "text", false, null));
        tableColumns.add(new TableColumn("program_id", "integer", false, null));
        tableColumns.add(new TableColumn("program_name", "text", false, null));
        existingTableData.setColumns(tableColumns);
        when(tableDataGenerator.getTableDataFromMart(formOneName.toLowerCase(),"SELECT * FROM formonename"))
                .thenReturn(existingTableData);

        formTableMetadataGenerator.addMetadataForForm(formOne);
        formTableMetadataGenerator.addMetadataForForm(formTwo);

        List<TableData> tableDataList = formTableMetadataGenerator.getTableDataList();

        assertNotNull(tableDataList);
        assertEquals(1, tableDataList.size());
        TableData actualTableData = tableDataList.get(0);

        assertNotNull(actualTableData);
        assertEquals(expected.getName(), actualTableData.getName());
        assertNotNull(actualTableData);
        assertEquals(10, actualTableData.getColumns().size());
        assertTrue(actualTableData.getColumns().containsAll(expected.getColumns()));
    }

    @Test
    public void shouldAddTableDataToMapWhenTableIsNotPresentInMartDatabase() {
        BahmniForm form = new BahmniForm();
        String formName = "formName";
        form.setFormName(new Concept(123, formName, 1));
        Concept field1 = new Concept(1, "field1", 0);
        field1.setDataType("int");
        Concept field2 = new Concept(2, "field2", 0);
        field2.setDataType("text");
        form.addField(field1);
        form.addField(field2);

        TableData expected = new TableData("formname");
        TableColumn tableColumnOne = new TableColumn("field1","int",false,null);
        TableColumn tableColumnTwo = new TableColumn("field2","text",false,null);
        expected.setColumns(Arrays.asList(tableColumnOne, tableColumnTwo));

        when(tableDataGenerator.getTableDataFromMart(formName.toLowerCase(),"SELECT * FROM formname"))
                .thenThrow(BadSqlGrammarException.class);

        formTableMetadataGenerator.addMetadataForForm(form);

        List<TableData> tableDataList = formTableMetadataGenerator.getTableDataList();
        assertNotNull(tableDataList);
        assertEquals(1, tableDataList.size());

        TableData actualTableData = tableDataList.get(0);
        assertNotNull(actualTableData);
        assertEquals(expected.getName(), actualTableData.getName());
        assertNotNull(actualTableData);
        assertEquals(10, actualTableData.getColumns().size());
        assertTrue(actualTableData.getColumns().containsAll(expected.getColumns()));
    }

    @Test
    public void shouldCallLoggerInfoInCatchBlockWheneverBadSqlGrammarExceptionThrown() throws Exception {
        Logger logger = mock(Logger.class);
        setValueForFinalStaticField(FormTableMetadataGenerator.class, "logger", logger);

        BahmniForm form = new BahmniForm();
        String formName = "formName";
        form.setFormName(new Concept(123, formName, 1));
        Concept field1 = new Concept(1, "field1", 0);
        field1.setDataType("int");
        form.addField(field1);

        when(tableDataGenerator.getTableDataFromMart(formName.toLowerCase(),"SELECT * FROM formname"))
                .thenThrow(BadSqlGrammarException.class);

        formTableMetadataGenerator.addMetadataForForm(form);

        verify(logger).info("formname table is not an existing table");
    }
}