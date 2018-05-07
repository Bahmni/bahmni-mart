package org.bahmni.mart;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BatchConfigurationIT extends AbstractBaseBatchIT {
    @Autowired
    private BatchConfiguration batchConfiguration;

    private Map<String, String> expectedPatientList;

    @Before
    public void setUp() throws Exception {
        super.setUp();
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
    }

    @Test
    @Sql(scripts = {"classpath:testDataSet/insertPatientDataWithDiagnoses.sql"},
            config = @SqlConfig(transactionManager = "customITContext"))
    public void shouldCreateTablesAndViewsBasedOnConfiguration() {
        batchConfiguration.run();

        List<Map<String, Object>> tables = martJdbcTemplate.queryForList("SELECT TABLE_NAME FROM " +
                "INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='public' AND TABLE_NAME <> 'test'");
        assertTrue(tables.size() >= 6);

        List<String> tableNames = tables.stream().map(table -> table.get("TABLE_NAME").toString())
                .collect(Collectors.toList());
        List<String> expectedTableNames = Arrays.asList("patient_allergy_status_test", "first_stage_validation",
                "fstg_specialty_determined_by_mlo", "follow_up_validation", "stage",
                "person_attributes", "bacteriology_concept_set", "visit_diagnoses");
        assertTrue(tableNames.containsAll(expectedTableNames));
        verifyTableColumns();

        List<Map<String, Object>> patientList = martJdbcTemplate
                .queryForList("SELECT * FROM \"patient_allergy_status_test\"");
        assertEquals(10, patientList.size());

        verifyRecords(patientList);
        verifyViews();
        verifyDiagnoses();
    }

    @Test
    public void shouldCreateTablesBasedOnJobConfigurationByIgnoringColumns() {
        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_allergy_status_test1'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());
        List<Map<String, Object>> patientList = martJdbcTemplate
                .queryForList("SELECT * FROM \"patient_allergy_status_test1\"");
        List<String> columnsName = tableDataColumns.stream().map(name -> name.toString().toLowerCase())
                .collect(Collectors.toList());

        assertEquals(10, patientList.size());
        assertEquals(2, tableDataColumns.size());
        assertTrue(columnsName.containsAll(Arrays.asList("patient_id", "allergy_status")));

        verifyRecords(patientList);
    }

    @Test
    public void shouldCreateTablesBasedOnTheSqlFilePathConfiguration() {
        batchConfiguration.run();

        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_details'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());
        List<String> columnsName = tableDataColumns.stream().map(name -> name.toString().toLowerCase())
                .collect(Collectors.toList());

        assertEquals(2, tableDataColumns.size());
        assertTrue(columnsName.containsAll(Arrays.asList("patient_id", "allergy_status")));
        verifyRecords(martJdbcTemplate.queryForList("SELECT * FROM \"patient_details\""));
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
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'patient_allergy_status_test_coded'" +
                " AND TABLE_SCHEMA='public';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        List<String> expectedColumns = Arrays.asList("patient_id", "allergy_status");

        assertEquals(2, tableDataColumns.size());
        assertTrue(tableDataColumns.containsAll(expectedColumns));
        List<Map<String, Object>> records = martJdbcTemplate.queryForList(
                "SELECT * FROM \"patient_allergy_status_test_coded\"");
        assertNotNull(records);
        assertFalse(records.isEmpty());
        verifyCodedPatientRecords(records);
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
        String rspViewSql = "SELECT * FROM registration_second_page_view";
        List<Map<String, Object>> view = martJdbcTemplate.queryForList("SELECT * from test_view");
        List<Map<String, Object>> viewFromFile = martJdbcTemplate.queryForList("SELECT * FROM view_from_file");
        List<Map<String, Object>> rspView = martJdbcTemplate.queryForList(rspViewSql);
        Set<String> columnNamesView = view.get(0).keySet();
        Set<String> columnNamesViewFromFile = viewFromFile.get(0).keySet();
        Set<String> rspViewColumns = rspView.get(0).keySet();
        List<String> rspViewExpectedCoulumns = Arrays.asList("rsp_fee_information_registration_fee",
                "rsp_nutritional_temp_height", "rsp_nutritional_weight", "patient_id", "encounter_id");

        assertEquals(2, columnNamesView.size());
        assertEquals(2, columnNamesViewFromFile.size());
        assertThat(Arrays.asList("patient_id", "allergy_status"), containsInAnyOrder(columnNamesView.toArray()));
        assertThat(Arrays.asList("patient_id", "allergy_status"),
                containsInAnyOrder(columnNamesViewFromFile.toArray()));
        assertEquals(10, view.size());
        assertEquals(10, viewFromFile.size());
        assertEquals(5, rspViewColumns.size());
        assertThat(rspViewExpectedCoulumns, containsInAnyOrder(rspViewColumns.toArray()));
        assertEquals(1, rspView.size());
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
        tableMap.put("patient_allergy_status_test", Arrays.asList("patient_id", "allergy_status"));
        tableMap.put("first_stage_validation",
                Arrays.asList("id_first_stage_validation", "patient_id", "encounter_id"));
        tableMap.put("fstg_specialty_determined_by_mlo",
                Arrays.asList("id_fstg_specialty_determined_by_mlo", "patient_id", "encounter_id",
                        "id_first_stage_validation", "fstg_specialty_determined_by_mlo"));
        tableMap.put("follow_up_validation", Arrays.asList("id_follow_up_validation", "patient_id", "encounter_id"));
        tableMap.put("stage", Arrays.asList("id_stage", "patient_id", "encounter_id", "id_first_stage_validation",
                "stage", "id_follow_up_validation"));
        tableMap.put("person_attributes", Arrays.asList("person_id", "givennamelocal", "familynamelocal",
                "middlenamelocal", "viber", "phonenumber2"));
        tableMap.put("bacteriology_concept_set", Arrays.asList("id_bacteriology_concept_set", "patient_id",
                "encounter_id", "specimen_sample_source", "specimen_id"));
        tableMap.put("visit_diagnoses", Arrays.asList("id_visit_diagnoses", "patient_id", "encounter_id",
                "non_coded_diagnosis", "coded_diagnosis", "diagnosis_certainty", "diagnosis_order",
                "bahmni_initial_diagnosis", "bahmni_diagnosis_revised", "bahmni_diagnosis_status"));

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
}