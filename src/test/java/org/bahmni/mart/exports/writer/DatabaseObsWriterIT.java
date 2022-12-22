package org.bahmni.mart.exports.writer;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
@Ignore
public class DatabaseObsWriterIT extends AbstractBaseBatchIT {

    @Autowired
    private DatabaseObsWriter databaseObsWriter;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @Test
    public void shouldWriteObsToDB() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setType("obs");

        BahmniForm bahmniForm = new BahmniForm();
        Concept formName = new Concept(1, "test", 1);
        formName.setDataType("N/A");
        bahmniForm.setFormName(formName);
        Concept fieldOne = new Concept(1, "field_one", 0);
        fieldOne.setDataType("Numeric");

        Concept fieldTwo = new Concept(1, "field_two", 0);
        fieldTwo.setDataType("Text");

        bahmniForm.addField(fieldOne);
        bahmniForm.addField(fieldTwo);

        ArrayList<ArrayList<Obs>> items = new ArrayList<>();
        ArrayList<Obs> obsList1 = new ArrayList<>();
        Obs obs1 = createObs("56", "2", "23", 1, 2, fieldOne, "4",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                null, null);

        Obs obs2 = createObs("56",  "2","23", 2, 2, fieldTwo, "test IT",
                null, "2018-12-2 09:07:32", "2018-12-3 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                null, null);

        Obs obs5 = createObs("56", "2",null, 23, 2, formName, null,
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                null, null);
        obsList1.add(obs1);
        obsList1.add(obs2);
        obsList1.add(obs5);

        ArrayList<Obs> obsList2 = new ArrayList<>();
        Obs obs3 = createObs("560", "2","3", 3, 2, fieldOne, "40",
                null, "2018-12-2 09:07:42", "2018-12-2 09:07:42", "2",
                "test location 2", "2", "test program 2", "9",
                null, null);

        Obs obs4 = createObs("560", "2","3", 4, 2, fieldTwo, "test IT 2",
                null, "2018-12-2 09:07:42", "2018-12-2 09:07:42", "2",
                "test location 2", "2", "test program 2", "9",
                null, null);
        obsList2.add(obs3);
        obsList2.add(obs4);

        items.add(obsList1);
        items.add(obsList2);

        formTableMetadataGenerator.addMetadataForForm(bahmniForm);
        databaseObsWriter.setForm(bahmniForm);
        databaseObsWriter.setJobDefinition(jobDefinition);
        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList(), jobDefinition);

        databaseObsWriter.write(items);

        List<Map<String, Object>> maps = martJdbcTemplate.queryForList("SELECT * FROM \"test\"");
        assertEquals(2, maps.size());

        Map<String, Object> actualObs1 = maps.get(0);
        assertEquals(14, actualObs1.size());
        assertEquals(1, actualObs1.get("id_test"));
        assertEquals(23, actualObs1.get("patient_id"));
        assertEquals(56, actualObs1.get("encounter_id"));
        assertEquals(2, actualObs1.get("visit_id"));
        assertEquals(new BigDecimal(4), actualObs1.get("field_one"));
        assertEquals("test IT", actualObs1.get("field_two"));
        assertEquals(1, actualObs1.get("location_id"));
        assertEquals("test location 1", actualObs1.get("location_name"));
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("date_created").toString());
        assertEquals("2018-12-03 09:07:32.0", actualObs1.get("date_modified").toString());
        assertEquals(1, actualObs1.get("program_id"));
        assertEquals("test program 1", actualObs1.get("program_name"));
        assertEquals(9, actualObs1.get("patient_program_id"));

        Map<String, Object> actualObs2 = maps.get(1);
        assertEquals(14, actualObs2.size());
        assertEquals(3, actualObs2.get("id_test"));
        assertEquals(3, actualObs2.get("patient_id"));
        assertEquals(560, actualObs2.get("encounter_id"));
        assertEquals(2, actualObs2.get("visit_id"));
        assertEquals(new BigDecimal(40), actualObs2.get("field_one"));
        assertEquals("test IT 2", actualObs2.get("field_two"));
        assertEquals(2, actualObs2.get("location_id"));
        assertEquals("test location 2", actualObs2.get("location_name"));
        assertEquals("2018-12-02 09:07:42.0", actualObs2.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:42.0", actualObs2.get("date_created").toString());
        assertNull(actualObs2.get("date_modified"));
        assertEquals(2, actualObs2.get("program_id"));
        assertEquals("test program 2", actualObs2.get("program_name"));
        assertEquals(9, actualObs2.get("patient_program_id"));
    }

    @Test
    public void shouldInsertRecordsForForm2Obs() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setType("form2obs");

        BahmniForm bahmniForm = new BahmniForm();
        Concept formName = new Concept();
        formName.setId(1);
        formName.setName("test");
        formName.setDataType("N/A");
        bahmniForm.setFormName(formName);

        Concept fieldOne = new Concept();
        fieldOne.setId(2);
        fieldOne.setName("field_one");
        fieldOne.setDataType("Numeric");

        Concept fieldTwo = new Concept();
        fieldTwo.setId(3);
        fieldTwo.setName("field_two");
        fieldTwo.setDataType("Text");

        bahmniForm.addField(fieldOne);
        bahmniForm.addField(fieldTwo);

        List<List<Obs>> items = new ArrayList<>();
        Obs obs1 = createObs("56", "2","23", 1, null, fieldOne, "4",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test", null);

        Obs obs2 = createObs("56", "2", "23", 2, null, fieldTwo, "test IT",
                null, "2018-12-2 09:07:32", "2018-12-3 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test", null);

        Obs obs3 = createObs("560", "2","3", 3, null, fieldOne, "40",
                null, "2018-12-2 09:07:42", "2018-12-2 09:07:42", "2",
                "test location 2", "2", "test program 2","9",
                "test", null);

        Obs obs4 = createObs("560", "2","3", 4, null, fieldTwo, "test IT 2",
                null, "2018-12-2 09:07:42", "2018-12-2 09:07:42", "2",
                "test location 2", "2", "test program 2", "9",
                "test", null);

        items.add(Arrays.asList(obs1, obs2));
        items.add(Arrays.asList(obs3, obs4));

        form2TableMetadataGenerator.addMetadataForForm(bahmniForm);
        databaseObsWriter.setForm(bahmniForm);
        databaseObsWriter.setJobDefinition(jobDefinition);
        tableGeneratorStep.createTables(form2TableMetadataGenerator.getTableDataList(), jobDefinition);

        databaseObsWriter.write(items);

        List<Map<String, Object>> maps = martJdbcTemplate.queryForList("SELECT * FROM \"test\"");
        assertEquals(2, maps.size());

        Map<String, Object> actualObs1 = maps.get(0);
        assertEquals(14, actualObs1.size());
        assertEquals("test", actualObs1.get("form_field_path"));
        assertEquals(23, actualObs1.get("patient_id"));
        assertEquals(56, actualObs1.get("encounter_id"));
        assertEquals(2, actualObs1.get("visit_id"));
        assertEquals(new BigDecimal(4), actualObs1.get("field_one"));
        assertEquals("test IT", actualObs1.get("field_two"));
        assertEquals(1, actualObs1.get("location_id"));
        assertEquals("test location 1", actualObs1.get("location_name"));
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("date_created").toString());
        assertEquals("2018-12-03 09:07:32.0", actualObs1.get("date_modified").toString());
        assertEquals(1, actualObs1.get("program_id"));
        assertEquals("test program 1", actualObs1.get("program_name"));
        assertEquals(9, actualObs1.get("patient_program_id"));


        Map<String, Object> actualObs2 = maps.get(1);
        assertEquals(14, actualObs2.size());
        assertEquals("test", actualObs2.get("form_field_path"));
        assertEquals(3, actualObs2.get("patient_id"));
        assertEquals(560, actualObs2.get("encounter_id"));
        assertEquals(2, actualObs2.get("visit_id"));
        assertEquals(new BigDecimal(40), actualObs2.get("field_one"));
        assertEquals("test IT 2", actualObs2.get("field_two"));
        assertEquals(2, actualObs2.get("location_id"));
        assertEquals("test location 2", actualObs2.get("location_name"));
        assertEquals("2018-12-02 09:07:42.0", actualObs2.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:42.0", actualObs2.get("date_created").toString());
        assertNull(actualObs2.get("date_modified"));
        assertEquals(2, actualObs2.get("program_id"));
        assertEquals("test program 2", actualObs2.get("program_name"));
        assertEquals(9, actualObs2.get("patient_program_id"));
    }

    @Test
    public void shouldInsertRecordsForForm2ObsForChildForm() {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setType("form2obs");

        BahmniForm bahmniForm = new BahmniForm();
        Concept formName = new Concept();
        formName.setId(1);
        formName.setName("section");
        formName.setDataType("N/A");
        bahmniForm.setFormName(formName);

        BahmniForm parentForm = new BahmniForm();
        Concept rootFormName = new Concept();
        rootFormName.setId(10);
        rootFormName.setName("test");
        rootFormName.setDataType("N/A");
        parentForm.setFormName(formName);
        bahmniForm.setParent(parentForm);

        Concept fieldOne = new Concept();
        fieldOne.setId(2);
        fieldOne.setName("field_one");
        fieldOne.setDataType("Numeric");

        Concept fieldTwo = new Concept();
        fieldTwo.setId(3);
        fieldTwo.setName("field_two");
        fieldTwo.setDataType("Text");

        bahmniForm.addField(fieldOne);
        bahmniForm.addField(fieldTwo);

        List<List<Obs>> items = new ArrayList<>();
        Obs obs1 = createObs("56", "2","23", 1, null, fieldOne, "4",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test.1/2-0", "test");

        Obs obs2 = createObs("56", "2","23", 2, null, fieldTwo, "test IT",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test.1/2-0", "test");

        Obs obs3 = createObs("560", "2","3", 3, null, fieldOne, "40",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test.1/2-1", "test");

        Obs obs4 = createObs("560", "2","3", 4, null, fieldTwo, "test IT 2",
                null, "2018-12-2 09:07:32", "2018-12-2 09:07:32", "1",
                "test location 1", "1", "test program 1", "9",
                "test.1/2-1", "test");

        items.add(Arrays.asList(obs1, obs2));
        items.add(Arrays.asList(obs3, obs4));

        form2TableMetadataGenerator.addMetadataForForm(bahmniForm);
        databaseObsWriter.setForm(bahmniForm);
        databaseObsWriter.setJobDefinition(jobDefinition);
        tableGeneratorStep.createTables(form2TableMetadataGenerator.getTableDataList(), jobDefinition);

        databaseObsWriter.write(items);

        List<Map<String, Object>> maps = martJdbcTemplate.queryForList("SELECT * FROM \"section\"");
        assertEquals(2, maps.size());

        Map<String, Object> actualObs1 = maps.get(0);
        assertEquals(15, actualObs1.size());
        assertEquals("test.1/2-0", actualObs1.get("form_field_path"));
        assertEquals("test", actualObs1.get("reference_form_field_path"));
        assertEquals(23, actualObs1.get("patient_id"));
        assertEquals(56, actualObs1.get("encounter_id"));
        assertEquals(2, actualObs1.get("visit_id"));
        assertEquals(new BigDecimal(4), actualObs1.get("field_one"));
        assertEquals("test IT", actualObs1.get("field_two"));
        assertEquals(1, actualObs1.get("location_id"));
        assertEquals("test location 1", actualObs1.get("location_name"));
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:32.0", actualObs1.get("date_created").toString());
        assertNull(actualObs1.get("date_modified"));
        assertEquals(1, actualObs1.get("program_id"));
        assertEquals("test program 1", actualObs1.get("program_name"));
        assertEquals(9, actualObs1.get("patient_program_id"));

        Map<String, Object> actualObs2 = maps.get(1);
        assertEquals(15, actualObs2.size());
        assertEquals("test.1/2-1", actualObs2.get("form_field_path"));
        assertEquals("test", actualObs2.get("reference_form_field_path"));
        assertEquals(3, actualObs2.get("patient_id"));
        assertEquals(560, actualObs2.get("encounter_id"));
        assertEquals(2, actualObs2.get("visit_id"));
        assertEquals(new BigDecimal(40), actualObs2.get("field_one"));
        assertEquals("test IT 2", actualObs2.get("field_two"));
        assertEquals(1, actualObs2.get("location_id"));
        assertEquals("test location 1", actualObs2.get("location_name"));
        assertEquals("2018-12-02 09:07:32.0", actualObs2.get("obs_datetime").toString());
        assertEquals("2018-12-02 09:07:32.0", actualObs2.get("date_created").toString());
        assertNull(actualObs2.get("date_modified"));
        assertEquals(1, actualObs2.get("program_id"));
        assertEquals("test program 1", actualObs2.get("program_name"));
        assertEquals(9, actualObs2.get("patient_program_id"));
    }

    private Obs createObs(String encounterId, String visitId, String patientId, Integer id, Integer parentId,
                          Concept field, String value, String parentName, String obsDateTime,
                          String dateCreated, String locationId, String locationName,
                          String programId, String programName, String patientProgramId, String formFieldPath,
                          String referenceFormFieldPath) {
        Obs obs = new Obs();
        obs.setEncounterId(encounterId);
        obs.setVisitId(visitId);
        obs.setPatientId(patientId);
        obs.setId(id);
        obs.setParentId(parentId);
        obs.setField(field);
        obs.setValue(value);
        obs.setParentName(parentName);
        obs.setObsDateTime(obsDateTime);
        obs.setDateCreated(dateCreated);
        obs.setLocationId(locationId);
        obs.setLocationName(locationName);
        obs.setProgramId(programId);
        obs.setProgramName(programName);
        obs.setPatientProgramId(patientProgramId);
        obs.setFormFieldPath(formFieldPath);
        obs.setReferenceFormFieldPath(referenceFormFieldPath);
        return obs;
    }
}