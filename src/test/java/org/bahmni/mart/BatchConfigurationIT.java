package org.bahmni.mart;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
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
    public void shouldCreateTablesAndViewsBasedOnConfiguration() {

        batchConfiguration.run();

        List<Map<String, Object>> tables = martJdbcTemplate.queryForList("SELECT TABLE_NAME FROM " +
                "INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='public' AND TABLE_NAME <> 'test'");
        assertTrue(tables.size() >= 6);

        List<String> tableNames = tables.stream().map(table -> table.get("TABLE_NAME").toString())
                .collect(Collectors.toList());
        List<String> expectedTableNames = Arrays.asList("patient_allergy_status_test", "first_stage_validation",
                "fstg,_specialty_determined_by_mlo", "follow-up_validation", "stage",
                "person_attributes", "bacteriology_concept_set");
        assertTrue(tableNames.containsAll(expectedTableNames));
        verifyTableColumns();

        List<Map<String, Object>> patientList = martJdbcTemplate
                .queryForList("SELECT * FROM \"patient_allergy_status_test\"");
        assertEquals(10, patientList.size());

        verifyRecords(patientList);
        verifyViews();
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

        List<String> expectedColumns = Arrays.asList("patient_id", "date_created", "encounter_id",
                "visit_name", "type_of_test", "panel_name", "test_name");

        assertEquals(7, tableDataColumns.size());
        assertTrue(tableDataColumns.containsAll(expectedColumns));
        verifyOrderRecords(martJdbcTemplate.queryForList("SELECT * FROM \"lab_samples\""), expectedColumns);
    }

    private void verifyOrderRecords(List<Map<String, Object>> actualOrders, List<String> expectedColumns) {
        Map<String, List> expectedOrders = new HashMap();
        expectedOrders.put("WBC (FBC)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "Clinic", "Hematology",
                "FBC (Full Blood Count)", "WBC (FBC)"));
        expectedOrders.put("RBC (FBC)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "Clinic", "Hematology",
                "FBC (Full Blood Count)", "RBC (FBC)"));
        expectedOrders.put("INR (HCS)", Arrays.asList("125", "2018-04-11 06:54:41.0", "100", "Clinic", "INR (HCS)",
                null, "INR (HCS)"));

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
        List<Map<String, Object>> view = martJdbcTemplate.queryForList("SELECT * from test_view");
        Set<String> columnNames = view.get(0).keySet();
        assertEquals(2, columnNames.size());
        assertThat(Arrays.asList("patient_id", "allergy_status"), containsInAnyOrder(columnNames.toArray()));
        assertEquals(10, view.size());
        verifyRecords(view);
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
        tableMap.put("fstg,_specialty_determined_by_mlo",
                Arrays.asList("id_fstg,_specialty_determined_by_mlo", "patient_id", "encounter_id",
                        "id_first_stage_validation", "fstg,_specialty_determined_by_mlo"));
        tableMap.put("follow-up_validation", Arrays.asList("id_follow-up_validation", "patient_id", "encounter_id"));
        tableMap.put("stage", Arrays.asList("id_stage", "patient_id", "encounter_id", "id_first_stage_validation",
                "stage", "id_follow-up_validation"));
        tableMap.put("person_attributes", Arrays.asList("person_id", "givennamelocal", "familynamelocal",
                "middlenamelocal", "viber", "phonenumber2"));
        tableMap.put("bacteriology_concept_set", Arrays.asList("id_bacteriology_concept_set", "patient_id",
                "encounter_id", "specimen_sample_source", "specimen_id"));

        for (String tableName : tableMap.keySet()) {
            String sql = String.format("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s' " +
                    "AND TABLE_SCHEMA='public';", tableName);
            List<String> columnNames = martJdbcTemplate.queryForList(sql).stream()
                    .map(columns -> columns.get("COLUMN_NAME").toString().toLowerCase()).collect(Collectors.toList());

            assertEquals(tableMap.get(tableName).size(), columnNames.size());
            assertTrue(tableMap.get(tableName).containsAll(columnNames));
        }
    }
}