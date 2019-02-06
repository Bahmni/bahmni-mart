package org.bahmni.mart;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BatchConfigurationIT extends AbstractBaseBatchIT {

    @Autowired
    private BatchConfiguration batchConfiguration;

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    private JdbcTemplate openmrsJdbcTemplate;

    private Map<String, String> expectedPatientList;

    private Map<String, Object> expectedFormGenericData;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        martJdbcTemplate.execute(getSqlFromResource("testDataSet/customSqlIncrementalUpdateData.sql"));
        martJdbcTemplate.execute(getSqlFromResource("testDataSet/eavIncrementalUpdateData.sql"));
        martJdbcTemplate.execute(getSqlFromResource("testDataSet/IncrementalUpdateData.sql"));
        martJdbcTemplate.execute(getSqlFromResource("testDataSet/ordersIncrementalUpdate.sql"));

        batchConfiguration.setShouldRunBatchJob(true);
        expectedPatientList = new HashMap<>();
        expectedPatientList.put("124", "Test");
        expectedPatientList.put("125", "Unknown");
        expectedPatientList.put("126", "Unknown");
        expectedPatientList.put("127", "Unknown");
        expectedPatientList.put("128", "Unknown");
        expectedPatientList.put("129", "Test 1");
        expectedPatientList.put("130", "Unknown");
        expectedPatientList.put("131", "Unknown");
        expectedPatientList.put("132", "Unknown");
        expectedPatientList.put("133", "Unknown");

        expectedFormGenericData = new HashMap<>();
        expectedFormGenericData.put("patient_id", 124);
        expectedFormGenericData.put("encounter_id", 22);
        expectedFormGenericData.put("program_id", 1);
        expectedFormGenericData.put("program_name", "Program Name");
        expectedFormGenericData.put("location_id", 8);
        expectedFormGenericData.put("location_name", null);
        expectedFormGenericData.put("obs_datetime", "2015-01-22 00:00:00.0");
        expectedFormGenericData.put("date_created", "2016-11-09 15:40:12.0");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        batchConfiguration.setShouldRunBatchJob(false);
    }

    @Test
    @Sql(scripts = {"classpath:testDataSet/insertPatientDataWithDiagnoses.sql"},
            config = @SqlConfig(transactionManager = "customOpenmrsITContext"))
    public void shouldCreateTablesAndViewsBasedOnConfiguration() {
        batchConfiguration.run();

        List<Map<String, Object>> tables = martJdbcTemplate.queryForList("SELECT TABLE_NAME FROM " +
                "INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='public' AND TABLE_NAME <> 'test'");
        assertTrue(tables.size() >= 6);

        List<String> tableNames = tables.stream().map(table -> table.get("TABLE_NAME").toString())
                .collect(Collectors.toList());
        List<String> expectedTableNames = Arrays.asList("patient_allergy_status_test_default", "first_stage_validation",
                "fstg_specialty_determined_by_mlo", "fstg_medical_files", "follow_up_validation", "stage",
                "person_attributes", "bacteriology_concept_set", "visit_diagnoses");
        assertTrue(tableNames.containsAll(expectedTableNames));
        verifyTableColumns();
        verifyObsRecords();

        List<Map<String, Object>> patientList = martJdbcTemplate
                .queryForList("SELECT * FROM \"patient_allergy_status_test_default\"");
        assertEquals(10, patientList.size());

        verifyRecords(patientList);
        verifyViews();
        verifyDiagnoses();
        verifyBacteriologyData(tableNames);
    }


    @Test
    public void shouldCreateTablesAndInsertDataForForm2Obs() throws IOException {
        String sql = String.format("INSERT INTO form_resource (form_resource_id, form_id, name, " +
                "value_reference, datatype, uuid) VALUES " +
                "(77, 78, 'FormBuilderForm', '%s', 'org.bahmni.customdatatype.datatype.FileSystemStorageDatatype', " +
                "'3f480fe5-da11-4c08-afad-5dc440515167')",
                System.getProperty("user.dir") + "/src/test/resources/form2MetadataJson/FormBuilderForm_1.json");

        openmrsJdbcTemplate.execute(sql);
        openmrsJdbcTemplate.execute(getSqlFromResource("testDataSet/Form2ObsData.sql"));

        batchConfiguration.run();

        List<Map<String, Object>> formRecords = martJdbcTemplate
                .queryForList("SELECT * FROM formbuilderform");
        List<Map<String, Object>> sectionRecords = martJdbcTemplate
                .queryForList("SELECT * FROM formbuilderform_section");
        List<Map<String, Object>> multiSelectRecords = martJdbcTemplate
                .queryForList("SELECT * FROM hi_penicillin");

        assertEquals(2, formRecords.size());
        Map<String, Object> formRecordOne = formRecords.get(0);
        Map<String, Object> formRecordTwo = formRecords.get(1);
        assertTrue(Arrays.asList("9", "4").containsAll(Arrays.asList(
                String.valueOf(formRecordOne.get("wwn_systolic_blood_pressure")),
                String.valueOf(formRecordTwo.get("wwn_systolic_blood_pressure")))));
        assertEquals("FormBuilderForm", String.valueOf(formRecordOne.get("form_field_path")));

        assertEquals(2, sectionRecords.size());
        Map<String, Object> sectionRecordOne = sectionRecords.get(0);
        Map<String, Object> sectionRecordTwo = sectionRecords.get(1);
        assertTrue(Arrays.asList("Notes 2", "Notes 1").containsAll(Arrays.asList(
                String.valueOf(sectionRecordOne.get("disposition_note")),
                String.valueOf(sectionRecordTwo.get("disposition_note")))));
        assertEquals("FormBuilderForm.1/2-0", String.valueOf(sectionRecordOne.get("form_field_path")));
        assertEquals("FormBuilderForm", String.valueOf(sectionRecordOne.get("reference_form_field_path")));
        assertEquals("FormBuilderForm.1/2-0", String.valueOf(sectionRecordTwo.get("form_field_path")));
        assertEquals("FormBuilderForm", String.valueOf(sectionRecordTwo.get("reference_form_field_path")));

        //TODO: Fix Multi Select primary key generation while creating table to allow inserting multi select data
//        assertEquals(3, multiSelectRecords.size());
//        Map<String, Object> multiSelectRecordOne = multiSelectRecords.get(0);
//        Map<String, Object> multiSelectRecordTwo = multiSelectRecords.get(1);
//        Map<String, Object> multiSelectRecordThree = multiSelectRecords.get(1);
//
//        assertTrue(Arrays.asList("Susceptible", "Resistant", "Intermediate").containsAll(Arrays.asList(
//                String.valueOf(multiSelectRecordOne.get("hi_penicillin")),
//                String.valueOf(multiSelectRecordTwo.get("hi_penicillin")),
//                String.valueOf(multiSelectRecordThree.get("hi_penicillin")))));
//        assertEquals("FormBuilderForm.1/2-0/4-0", String.valueOf(multiSelectRecordOne.get("form_field_path")));
//        assertEquals("FormBuilderForm.1/2-0", String.valueOf(multiSelectRecordOne.get("reference_form_field_path")));
//        assertEquals("FormBuilderForm.1/2-0/4-0", String.valueOf(multiSelectRecordTwo.get("form_field_path")));
//        assertEquals("FormBuilderForm.1/2-0", String.valueOf(multiSelectRecordTwo.get("reference_form_field_path")));
//        assertEquals("FormBuilderForm.1/2-0/4-0", String.valueOf(multiSelectRecordThree.get("form_field_path")));
//        assertEquals("FormBuilderForm.1/2-0",
//                String.valueOf(multiSelectRecordThree.get("reference_form_field_path")));
    }

    @Test
    public void shouldCreateTablesBasedOnJobConfigurationByIgnoringColumns() {
        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_allergy_status_test1_default'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());
        List<Map<String, Object>> patientList = martJdbcTemplate
                .queryForList("SELECT * FROM \"patient_allergy_status_test1_default\"");
        List<String> columnsName = tableDataColumns.stream().map(name -> name.toString().toLowerCase())
                .collect(Collectors.toList());

        assertEquals(10, patientList.size());
        assertEquals(2, tableDataColumns.size());
        assertTrue(columnsName.containsAll(Arrays.asList("patient_id", "allergy_status")));

        verifyRecords(patientList);
    }

    @Test
    public void shouldCreateTablesBasedOnTheSourceFilePathConfiguration() {
        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_details_default'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());
        List<String> columnsName = tableDataColumns.stream().map(name -> name.toString().toLowerCase())
                .collect(Collectors.toList());

        assertEquals(2, tableDataColumns.size());
        assertTrue(columnsName.containsAll(Arrays.asList("patient_id", "allergy_status")));
        verifyRecords(martJdbcTemplate.queryForList("SELECT * FROM \"patient_details_default\""));
    }

    @Test
    public void shouldCreateOrderLabSamplesTableByIgnoringVisitId() {

        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'lab_samples'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        List<String> expectedColumns = Arrays.asList("patient_id", "date_created", "encounter_id", "encounter_type_id",
                "encounter_type_name", "visit_type_id", "visit_type", "type_of_test", "panel_name", "test_name");

        assertEquals(10, tableDataColumns.size());
        assertTrue(tableDataColumns.containsAll(expectedColumns));
        verifyOrderRecords(martJdbcTemplate.queryForList("SELECT * FROM \"lab_samples\""), expectedColumns);
    }

    @Test
    public void shouldCreateCustomCodesTableWithDataFromCSV() throws Exception {
        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'custom_codes'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        List<String> expectedColumns = Arrays.asList("name", "source", "type", "code");

        assertEquals(4, tableDataColumns.size());
        assertTrue(tableDataColumns.containsAll(expectedColumns));
        List<Map<String, Object>> records = martJdbcTemplate.queryForList("SELECT * FROM \"custom_codes\"");
        assertNotNull(records);
        assertFalse(records.isEmpty());
        verifyCustomCodesRecords(records, expectedColumns);
    }

    @Test
    public void shouldAddCodesToNonConceptsGiveValidCodeConfigs() {
        batchConfiguration.run();
        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_allergy_status_test_coded_default'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        List<String> expectedColumns = Arrays.asList("patient_id", "allergy_status");

        assertEquals(2, tableDataColumns.size());
        assertTrue(tableDataColumns.containsAll(expectedColumns));
        List<Map<String, Object>> records = martJdbcTemplate.queryForList(
                "SELECT * FROM \"patient_allergy_status_test_coded_default\"");
        assertNotNull(records);
        assertFalse(records.isEmpty());
        verifyCodedPatientRecords(records);
    }

    @Test
    public void shouldCreateProcedureFromSourceFile() throws Exception {
        batchConfiguration.run();

        List<Map<String, Object>> procRecords = martJdbcTemplate.queryForList(
                "SELECT patient_id,getAllergyStatus(patient_id) AS allergy_status FROM " +
                        "\"patient_allergy_status_test_default\"");

        verifyRecords(procRecords);
    }

    @Test
    public void shouldUpdateEventRecordIdInMarkerTable() {
        String maxEventRecordId = getMaxEventRecordId();
        String jobName = "Obs Data";

        batchConfiguration.run();

        assertEquals(maxEventRecordId, getEventRecordIdForJob(jobName));
        List<Map<String, Object>> healthEducationData = martJdbcTemplate.queryForList("SELECT * FROM health_education");
        assertNotNull(healthEducationData);
        assertEquals(3, healthEducationData.size());
    }

    @Test
    public void shouldApplyIncrementalUpdateForObsData() throws IOException {
        batchConfiguration.run();

        List<Map<String, Object>> healthEducationData = martJdbcTemplate.queryForList("SELECT * FROM health_education");

        assertNotNull(healthEducationData);
        assertEquals(3, healthEducationData.size());

        assertEquals(58658, healthEducationData.get(0).get("encounter_id"));
        assertEquals("Married", healthEducationData.get(0).get("he_marital_status"));
        assertEquals("No formal education", healthEducationData.get(0).get("he_highest_education_level"));
        assertEquals("False", healthEducationData.get(0).get("he_pregnancy_status"));

        assertEquals(58638, healthEducationData.get(1).get("encounter_id"));
        assertEquals("Married", healthEducationData.get(1).get("he_marital_status"));
        assertEquals("No formal education", healthEducationData.get(1).get("he_highest_education_level"));
        assertEquals("True", healthEducationData.get(1).get("he_pregnancy_status"));

        assertEquals(58639, healthEducationData.get(2).get("encounter_id"));
        assertEquals("Single", healthEducationData.get(2).get("he_marital_status"));
        assertEquals("True", healthEducationData.get(2).get("he_pregnancy_status"));
    }


    @Test
    public void shouldApplyIncrementalUpdateForCustomSqlJob() {
        String maxEventRecordId = getMaxEventRecordId();

        batchConfiguration.run();

        String jobName = "Patient program data";
        assertEquals(maxEventRecordId, getEventRecordIdForJob(jobName));
        List<Map<String, Object>> actualPatientProgramData =
                martJdbcTemplate.queryForList("SELECT * FROM patient_program_data_default");
        assertEquals(3, actualPatientProgramData.size());
        List<Map<String, Object>> expectedPatientProgramData = getExpectedPatientProgramTableData();
        assertTrue(expectedPatientProgramData.containsAll(actualPatientProgramData));
    }

    @Test
    public void shouldApplyIncrementalUpdateForEavJob() {
        String maxEventRecordId = getMaxEventRecordId();

        batchConfiguration.run();

        String jobName = "Test Person Attributes";
        assertEquals(maxEventRecordId, getEventRecordIdForJob(jobName));

        List<Map<String, Object>> actualPatientProgramData =
                martJdbcTemplate.queryForList("SELECT * FROM test_person_attributes");
        assertEquals(3, actualPatientProgramData.size());
        List<Map<String, Object>> expectedPatientProgramData = getExpectedTestPersonAttributeTableData();
        assertTrue(expectedPatientProgramData.containsAll(actualPatientProgramData));
    }

    @Test
    public void shouldApplyIncrementalUpdateForOrdersJob() throws Exception {
        String maxEventRecordId = getMaxEventRecordId();

        batchConfiguration.run();

        String jobName = "Orders Data";
        assertEquals(maxEventRecordId, getEventRecordIdForJob(jobName));

        List<Map<String, Object>> actualOrdersData =
                martJdbcTemplate.queryForList("SELECT * FROM radiology_orders ORDER BY test_name");
        assertEquals(2, actualOrdersData.size());
        List<Map<String, Object>> expectedOrdersData = getOrdersData();
        verifyOrdersIncrementalData(expectedOrdersData, actualOrdersData);
    }

    private void verifyOrdersIncrementalData(List<Map<String, Object>> expectedOrdersData,
                                             List<Map<String, Object>> actualOrdersData) {
        for (int index = 0; index < actualOrdersData.size(); index++) {
            Map<String, Object> actualOrdersDataRow = actualOrdersData.get(index);
            for (String key : actualOrdersDataRow.keySet()) {
                Map<String, Object> expectedOrderDataRow = expectedOrdersData.get(index);
                assertEquals(String.valueOf(expectedOrderDataRow.get(key)),
                        String.valueOf(actualOrdersDataRow.get(key)));
            }
        }
    }

    private List<Map<String, Object>> getOrdersData() {
        HashMap<String, Object> row1 = new HashMap<String, Object>() {
            {
                put("patient_id", 133);
                put("date_created", "2018-07-18 05:47:21.0");
                put("encounter_id", 14);
                put("encounter_type_id", 1);
                put("encounter_type_name", "Consultation");
                put("visit_type", "Clinic");
                put("visit_type_id", 4);
                put("type_of_test", "All_Tests_and_Panels");
                put("panel_name", "Anaemia Panel");
                put("test_name", "MCH");
            }
        };
        HashMap<String, Object> row2 = new HashMap<String, Object>() {
            {
                put("patient_id", 133);
                put("date_created", "2018-07-19 06:54:41.0");
                put("encounter_id", 18);
                put("encounter_type_id", 1);
                put("encounter_type_name", "Consultation");
                put("visit_type", "Clinic");
                put("visit_type_id", 4);
                put("type_of_test", "Radiology Orders");
                put("panel_name", "Radiology Test");
            }
        };
        return Arrays.asList(row1, row2);
    }

    private String getSqlFromResource(String resourceName) throws IOException {
        URL resource = this.getClass().getClassLoader().getResource(resourceName);

        return FileUtils.readFileToString(new File(resource.getFile()));
    }

    private List<Map<String, Object>> getExpectedTestPersonAttributeTableData() {
        HashMap<String, Object> oldRow = new HashMap<String, Object>() {
            {
                put("person_id", 124);
                put("givenNameLocal", "Superman");
                put("familyNameLocal", "Superhero");
                put("middleNameLocal", null);
                put("phoneNumber2", "000000000");
                put("viber", null);
            }
        };
        HashMap<String, Object> editedRow = new HashMap<String, Object>() {
            {
                put("person_id", 125);
                put("givenNameLocal", "Hulk");
                put("familyNameLocal", null);
                put("middleNameLocal", null);
                put("phoneNumber2", null);
                put("viber", null);
            }
        };
        HashMap<String, Object> newlyInsertedRow = new HashMap<String, Object>() {
            {
                put("person_id", 126);
                put("givenNameLocal", null);
                put("familyNameLocal", "Captain America");
                put("middleNameLocal", null);
                put("phoneNumber2", null);
                put("viber", null);
            }
        };
        return Arrays.asList(oldRow, editedRow, newlyInsertedRow);
    }

    private String getMaxEventRecordId() {
        return openmrsJdbcTemplate.queryForObject("SELECT MAX(id) FROM event_records", String.class);
    }

    private List<Map<String, Object>> getExpectedPatientProgramTableData() {
        HashMap<String, Object> oldRow = new HashMap<String, Object>() {
            {
                put("patient_program_id", 2);
                put("patient_id", 125);
            }
        };
        HashMap<String, Object> editedRow = new HashMap<String, Object>() {
            {
                put("patient_program_id", 1);
                put("patient_id", 126);
            }
        };
        HashMap<String, Object> newlyInsertedRow = new HashMap<String, Object>() {
            {
                put("patient_program_id", 3);
                put("patient_id", 127);
            }
        };
        return Arrays.asList(oldRow, editedRow, newlyInsertedRow);
    }

    private void verifyObsRecords() {
        List<Map<String, Object>> parentForm = martJdbcTemplate
                .queryForList("SELECT * FROM first_stage_validation");
        List<Map<String, Object>> childForm = martJdbcTemplate.queryForList("SELECT * FROM fstg_medical_files");
        List<Map<String, Object>> grandChildForm = martJdbcTemplate
                .queryForList("SELECT * FROM fstg_specialty_determined_by_mlo");

        assertNotNull(parentForm);
        assertEquals(1, parentForm.size());
        Map<String, Object> parentFormData = parentForm.get(0);
        assertEquals(26, parentFormData.get("id_first_stage_validation"));
        verifyGenericFormData(parentFormData);

        assertNotNull(childForm);
        assertEquals(1, childForm.size());
        Map<String, Object> childFormData = childForm.get(0);
        assertEquals(27, childFormData.get("id_fstg_medical_files"));
        assertEquals(26, childFormData.get("id_first_stage_validation"));
        verifyGenericFormData(childFormData);

        assertNotNull(grandChildForm);
        assertEquals(1, grandChildForm.size());
        Map<String, Object> grandChildData = grandChildForm.get(0);
        assertEquals(28, grandChildData.get("id_fstg_specialty_determined_by_mlo"));
        assertEquals(27, grandChildData.get("id_fstg_medical_files"));
        verifyGenericFormData(grandChildData);
    }

    private void verifyGenericFormData(Map<String, Object> formDetails) {
        assertEquals(expectedFormGenericData.get("patient_id"), formDetails.get("patient_id"));
        assertEquals(expectedFormGenericData.get("encounter_id"), formDetails.get("encounter_id"));
        assertEquals(expectedFormGenericData.get("program_id"), formDetails.get("program_id"));
        assertEquals(expectedFormGenericData.get("program_name"), formDetails.get("program_name"));
        assertEquals(expectedFormGenericData.get("location_id"), formDetails.get("location_id"));
        assertEquals(expectedFormGenericData.get("location_name"), formDetails.get("location_name"));
        assertEquals(expectedFormGenericData.get("obs_datetime"), formDetails.get("obs_datetime").toString());
        assertEquals(expectedFormGenericData.get("date_created"), formDetails.get("date_created").toString());
    }

    private void verifyCodedPatientRecords(List<Map<String, Object>> records) {
        Set<String> expected = new HashSet<>(Arrays.asList("unknown101", "Test", "Test 1"));
        Set<String> actualCodes = new HashSet<>();
        for (Map<String, Object> record : records) {
            actualCodes.add((String) record.get("allergy_status"));
        }
        assertTrue(expected.containsAll(actualCodes));
    }

    private void verifyCustomCodesRecords(List<Map<String, Object>> records, List<String> expectedColumns) {
        List<Map<String, Object>> expectedRecords = new ArrayList<>();
        Map<String, Object> firstRecord = new HashMap<>();
        firstRecord.put(expectedColumns.get(0), "Unknown");
        firstRecord.put(expectedColumns.get(1), "bahmni");
        firstRecord.put(expectedColumns.get(2), "patient_info");
        firstRecord.put(expectedColumns.get(3), "unknown101");
        expectedRecords.add(firstRecord);
        for (int index = 0; index < records.size(); index++) {
            int finalIndex = index;
            expectedColumns.forEach(column ->
                    assertEquals(expectedRecords.get(finalIndex).get(column), records.get(finalIndex).get(column))
            );
        }
    }

    private void verifyOrderRecords(List<Map<String, Object>> actualOrders, List<String> expectedColumns) {
        Map<String, List> expectedOrders = new HashMap<>();
        expectedOrders.put("WBC (FBC)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "1", "Consultation", "4",
                "Clinic", "Hematology", "FBC (Full Blood Count)", "WBC (FBC)"));
        expectedOrders.put("RBC (FBC)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "1", "Consultation", "4",
                "Clinic", "Hematology", "FBC (Full Blood Count)", "RBC (FBC)"));
        expectedOrders.put("INR (HCS)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "1", "Consultation", "4",
                "Clinic", "INR (HCS)", null, "INR (HCS)"));

        for (Map<String, Object> row : actualOrders) {
            List<String> actualOrder = new ArrayList<>();
            for (String column : expectedColumns) {
                actualOrder.add(row.get(column) != null ? row.get(column).toString() : null);
            }
            List<String> expectedOrder = expectedOrders.get(row.get("test_name"));
            assertEquals(expectedOrder.size(), actualOrder.size());
            assertTrue(expectedOrder.containsAll(actualOrder));
        }
    }

    private void verifyViews() {
        String regViewSql = "SELECT * FROM registration_second_page_view";
        List<Map<String, Object>> view = martJdbcTemplate.queryForList("SELECT * from test_view");
        List<Map<String, Object>> viewFromFile = martJdbcTemplate.queryForList("SELECT * FROM view_from_file");
        List<Map<String, Object>> regView = martJdbcTemplate.queryForList(regViewSql);
        Set<String> columnNamesView = view.get(0).keySet();
        Set<String> columnNamesViewFromFile = viewFromFile.get(0).keySet();
        Set<String> regViewColumns = regView.get(0).keySet();
        List<String> regViewExpectedColumns = Arrays.asList("reg_fee_information_registration_fee",
                "reg_nutritional_temp_height", "reg_nutritional_weight", "patient_id", "encounter_id", "obs_datetime",
                "date_created", "date_modified", "location_id", "location_name", "program_id", "program_name");

        assertEquals(2, columnNamesView.size());
        assertEquals(2, columnNamesViewFromFile.size());
        assertThat(Arrays.asList("patient_id", "allergy_status"), containsInAnyOrder(columnNamesView.toArray()));
        assertThat(Arrays.asList("patient_id", "allergy_status"),
                containsInAnyOrder(columnNamesViewFromFile.toArray()));
        assertEquals(10, view.size());
        assertEquals(10, viewFromFile.size());
        assertEquals(12, regViewColumns.size());
        assertThat(regViewExpectedColumns, containsInAnyOrder(regViewColumns.toArray()));
        assertEquals(1, regView.size());
        verifyRecords(view);
        verifyRecords(viewFromFile);
    }

    private void verifyRecords(List<Map<String, Object>> patientList) {
        for (Map<String, Object> row : patientList) {
            String patientId = row.get("patient_id").toString();
            String allergyStatus = row.get("allergy_status").toString();
            assertEquals(expectedPatientList.get(patientId), allergyStatus);
        }
    }

    private void verifyTableColumns() {
        HashMap<String, List<String>> tableMap = new HashMap<>();
        tableMap.put("patient_allergy_status_test_default", Arrays.asList("patient_id", "allergy_status"));

        tableMap.put("first_stage_validation",
                Arrays.asList("id_first_stage_validation", "patient_id", "encounter_id",
                        "obs_datetime", "date_created", "date_modified", "location_id", "location_name", "program_id",
                        "program_name"));

        tableMap.put("fstg_medical_files",
                Arrays.asList("id_fstg_medical_files", "id_first_stage_validation", "patient_id", "encounter_id",
                        "obs_datetime", "date_created", "date_modified", "location_id", "location_name", "program_id",
                        "program_name"));

        tableMap.put("fstg_specialty_determined_by_mlo",
                Arrays.asList("id_fstg_specialty_determined_by_mlo", "patient_id", "encounter_id",
                        "fstg_specialty_determined_by_mlo", "obs_datetime", "date_created", "date_modified",
                        "location_id", "location_name", "program_id", "program_name", "id_fstg_medical_files"));

        tableMap.put("follow_up_validation", Arrays.asList("id_follow_up_validation", "patient_id", "encounter_id",
                "obs_datetime", "date_created", "date_modified", "location_id", "location_name", "program_id",
                "program_name"));

        tableMap.put("stage", Arrays.asList("id_stage", "patient_id", "encounter_id", "id_fstg_medical_files",
                "stage", "id_follow_up_validation", "obs_datetime", "date_created", "date_modified", "location_id",
                "location_name", "program_id", "program_name"));

        tableMap.put("person_attributes", Arrays.asList("person_id", "givennamelocal", "familynamelocal",
                "middlenamelocal", "viber", "phonenumber2"));

        tableMap.put("bacteriology_concept_set", Arrays.asList("id_bacteriology_concept_set", "patient_id",
                "encounter_id", "specimen_sample_source", "specimen_id",
                "obs_datetime", "date_created", "date_modified", "location_id", "location_name", "program_id",
                "program_name"));

        tableMap.put("visit_diagnoses", Arrays.asList("id_visit_diagnoses", "patient_id", "encounter_id",
                "non_coded_diagnosis", "coded_diagnosis", "diagnosis_certainty", "diagnosis_order",
                "bahmni_initial_diagnosis", "bahmni_diagnosis_revised", "bahmni_diagnosis_status",
                "obs_datetime", "date_created", "date_modified", "location_id", "location_name", "program_id",
                "program_name"));

        for (String tableName : tableMap.keySet()) {
            String sql = String.format("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s' " +
                    "AND TABLE_SCHEMA='public';", tableName);
            List<String> columnNames = martJdbcTemplate.queryForList(sql).stream()
                    .map(columns -> columns.get("COLUMN_NAME").toString().toLowerCase()).collect(Collectors.toList());

            assertEquals(tableMap.get(tableName).size(), columnNames.size());
            assertTrue(tableMap.get(tableName).containsAll(columnNames));
        }
    }

    private void verifyDiagnoses() {
        HashMap<String, Object> row1 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 13);
                put("patient_id", 129);
                put("encounter_id", 2);
                put("non_coded_diagnosis", "test diagnoses");
                put("coded_diagnosis", null);
                put("diagnosis_certainty", "Confirmed");
                put("diagnosis_order", "Primary");
                put("bahmni_initial_diagnosis", "7e4a4370-c9b2-4cf2-8c29-84756cc67dd5");
                put("bahmni_diagnosis_revised", "False");
                put("bahmni_diagnosis_status", null);
            }
        };

        HashMap<String, Object> row2 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 96);
                put("patient_id", 133);
                put("encounter_id", 8);
                put("non_coded_diagnosis", null);
                put("coded_diagnosis", "Dyspareunia");
                put("diagnosis_certainty", "Presumed");
                put("diagnosis_order", "Secondary");
                put("bahmni_initial_diagnosis", "16a07ae8-9c9f-4737-a1fa-f96261a5783b");
                put("bahmni_diagnosis_revised", "True");
                put("bahmni_diagnosis_status", null);
            }
        };

        HashMap<String, Object> row3 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 123);
                put("patient_id", 133);
                put("encounter_id", 10);
                put("non_coded_diagnosis", null);
                put("coded_diagnosis", "Dyspareunia");
                put("diagnosis_certainty", "Confirmed");
                put("diagnosis_order", "Secondary");
                put("bahmni_initial_diagnosis", "16a07ae8-9c9f-4737-a1fa-f96261a5783b");
                put("bahmni_diagnosis_revised", "True");
                put("bahmni_diagnosis_status", null);
            }
        };

        HashMap<String, Object> row4 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 130);
                put("patient_id", 125);
                put("encounter_id", 11);
                put("non_coded_diagnosis", null);
                put("coded_diagnosis", "Dyspareunia");
                put("diagnosis_certainty", "Confirmed");
                put("diagnosis_order", "Primary");
                put("bahmni_initial_diagnosis", "48114fe3-4464-41cf-9cc0-2a4ba2be0c1b");
                put("bahmni_diagnosis_revised", "False");
                put("bahmni_diagnosis_status", null);
            }
        };

        HashMap<String, Object> row5 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 136);
                put("patient_id", 133);
                put("encounter_id", 12);
                put("non_coded_diagnosis", null);
                put("coded_diagnosis", "Dyspareunia");
                put("diagnosis_certainty", "Presumed");
                put("diagnosis_order", "Primary");
                put("bahmni_initial_diagnosis", "16a07ae8-9c9f-4737-a1fa-f96261a5783b");
                put("bahmni_diagnosis_revised", "False");
                put("bahmni_diagnosis_status", null);
            }
        };

        HashMap<String, Object> row6 = new HashMap<String, Object>() {
            {
                put("id_visit_diagnoses", 143);
                put("patient_id", 132);
                put("encounter_id", 13);
                put("non_coded_diagnosis", null);
                put("coded_diagnosis", "Parkinson");
                put("diagnosis_certainty", "Presumed");
                put("diagnosis_order", "Secondary");
                put("bahmni_initial_diagnosis", "7b1010dc-0f7d-4574-a1c9-77f82d9c2953");
                put("bahmni_diagnosis_revised", "False");
                put("bahmni_diagnosis_status", "Ruled Out Diagnosis");
            }
        };

        HashMap<Integer, HashMap<String, Object>> ledger = new HashMap<Integer, HashMap<String, Object>>() {
            {
                put(13, row1);
                put(96, row2);
                put(123, row3);
                put(130, row4);
                put(136, row5);
                put(143, row6);
            }
        };

        Object totalRow = martJdbcTemplate
                .queryForList("SELECT count(*) AS total_row FROM visit_diagnoses").get(0).get("total_row");

        assertEquals((long) ledger.size(), totalRow);

        for (Integer visitDiagnosesId : ledger.keySet()) {
            Map<String, Object> actualRow = martJdbcTemplate.queryForList(String
                    .format("SELECT * FROM visit_diagnoses WHERE id_visit_diagnoses = %d", visitDiagnosesId)).get(0);

            HashMap<String, Object> expectedRow = ledger.get(visitDiagnosesId);

            for (String columnName : expectedRow.keySet())
                assertEquals(expectedRow.get(columnName), actualRow.get(columnName));
        }
    }

    private void verifyBacteriologyData(List<String> tableNames) {

        verifyNoSeparateTableForSpecimenSampleSource(tableNames);

        List<Map<String, Object>> bacteriologyRows = martJdbcTemplate
                .queryForList("select * from bacteriology_concept_set");

        assertEquals(2, bacteriologyRows.size());

        Map<String, Object> firstRow = bacteriologyRows.get(0);
        Map<String, Object> secondRow = bacteriologyRows.get(1);

        assertEquals(12, firstRow.size());
        assertEquals(12, secondRow.size());

        assertEquals(1, firstRow.get("specimen_sample_source"));
        assertEquals(2, secondRow.get("specimen_sample_source"));

        bacteriologyRows.forEach(row -> {

            assertEquals(2820, row.get("id_bacteriology_concept_set"));
            assertEquals(124, row.get("patient_id"));
            assertEquals(22, row.get("encounter_id"));
            assertEquals("2015-01-22 00:00:00.0", row.get("obs_datetime").toString());
            assertEquals("2016-11-09 15:40:12.0", row.get("date_created").toString());
            assertEquals(null, row.get("date_modified"));
            assertEquals(8, row.get("location_id"));
            assertNull(row.get("location_name"));
            assertEquals(1, row.get("program_id"));
            assertEquals("Program Name", row.get("program_name"));
            assertNull(row.get("specimen_id"));

        });
    }

    /**
     * No separate table for "Specimen Sample Source" even though it's a
     * multi-select(defaultApp.json) since "enableForAddMoreAndMultiSelect"
     * flag is false for bacteriology job
     *
     * @param tableNames table names in mart test database
     */
    private void verifyNoSeparateTableForSpecimenSampleSource(List<String> tableNames) {
        assertFalse(tableNames.contains("specimen_sample_source"));
    }

    private String getEventRecordIdForJob(String jobName) {
        return martJdbcTemplate.queryForObject(String.format(
                "SELECT event_record_id FROM markers WHERE job_name = '%s'", jobName), String.class);
    }
}
