package org.bahmni.mart.exports.writer;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DatabaseObsWriterIT extends AbstractBaseBatchIT {

    @Autowired
    private DatabaseObsWriter databaseObsWriter;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Test
    public void shouldWriteObsToDB() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();

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
        Obs obs1 = new Obs(1, 2, fieldOne, "4");
        obs1.setEncounterId("56");
        obs1.setPatientId("23");
        obs1.setLocationId("1");
        obs1.setLocationName("test location 1");
        obs1.setObsDateTime("2018-12-2 09:07:32");
        obs1.setDateCreated("2018-12-2 09:07:32");
        obs1.setProgramId("1");
        obs1.setProgramName("test program 1");

        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");
        obs2.setPatientId("23");
        obs2.setLocationId("1");
        obs2.setLocationName("test location 1");
        obs2.setObsDateTime("2018-12-2 09:07:32");
        obs2.setDateCreated("2018-12-3 09:07:32");
        obs2.setProgramId("1");
        obs2.setProgramName("test program 1");

        Obs obs5 = new Obs(23, 2, formName, null);
        obs5.setEncounterId("56");
        obs5.setLocationId("1");
        obs5.setLocationName("test location 1");
        obs5.setObsDateTime("2018-12-2 09:07:32");
        obs5.setDateCreated("2018-12-2 09:07:32");
        obs5.setProgramId("1");
        obs5.setProgramName("test program 1");

        obsList1.add(obs1);
        obsList1.add(obs2);
        obsList1.add(obs5);

        ArrayList<Obs> obsList2 = new ArrayList<>();
        Obs obs3 = new Obs(3, 2, fieldOne, "40");
        obs3.setEncounterId("560");
        obs3.setPatientId("3");
        obs3.setLocationId("2");
        obs3.setLocationName("test location 2");
        obs3.setObsDateTime("2018-12-2 09:07:42");
        obs3.setDateCreated("2018-12-2 09:07:42");
        obs3.setProgramId("2");
        obs3.setProgramName("test program 2");

        Obs obs4 = new Obs(4, 2, fieldTwo, "test IT 2");
        obs4.setEncounterId("560");
        obs4.setPatientId("3");
        obs4.setLocationId("2");
        obs4.setLocationName("test location 2");
        obs4.setObsDateTime("2018-12-2 09:07:42");
        obs4.setDateCreated("2018-12-2 09:07:42");
        obs4.setProgramId("2");
        obs4.setProgramName("test program 2");

        obsList2.add(obs3);
        obsList2.add(obs4);

        items.add(obsList1);
        items.add(obsList2);

        formTableMetadataGenerator.addMetadataForForm(bahmniForm);
        databaseObsWriter.setForm(bahmniForm);
        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList(), jobDefinition);

        databaseObsWriter.write(items);

        List<Map<String, Object>> maps = martJdbcTemplate.queryForList("SELECT * FROM \"test\"");
        assertEquals(2, maps.size());

        Map<String, Object> actualObs1 = maps.get(0);
        assertEquals(12, actualObs1.size());
        assertEquals(1, actualObs1.get("id_test"));
        assertEquals(23, actualObs1.get("patient_id"));
        assertEquals(56, actualObs1.get("encounter_id"));
        assertEquals(new BigDecimal(4), actualObs1.get("field_one"));
        assertEquals("test IT", actualObs1.get("field_two"));
        assertEquals(1, actualObs1.get("location_id"));
        assertEquals("test location 1", actualObs1.get("location_name"));
        assertEquals("2018-12-2 09:07:32", actualObs1.get("obs_datetime"));
        assertEquals("2018-12-2 09:07:32", actualObs1.get("date_created"));
        assertEquals("2018-12-03 09:07:32.0", actualObs1.get("date_modified").toString());
        assertEquals(1, actualObs1.get("program_id"));
        assertEquals("test program 1", actualObs1.get("program_name"));

        Map<String, Object> actualObs2 = maps.get(1);
        assertEquals(12, actualObs2.size());
        assertEquals(3, actualObs2.get("id_test"));
        assertEquals(3, actualObs2.get("patient_id"));
        assertEquals(560, actualObs2.get("encounter_id"));
        assertEquals(new BigDecimal(40), actualObs2.get("field_one"));
        assertEquals("test IT 2", actualObs2.get("field_two"));
        assertEquals("test IT", actualObs1.get("field_two"));
        assertEquals(2, actualObs2.get("location_id"));
        assertEquals("test location 2", actualObs2.get("location_name"));
        assertEquals("2018-12-2 09:07:42", actualObs2.get("obs_datetime"));
        assertEquals("2018-12-2 09:07:42", actualObs2.get("date_created"));
        assertNull(actualObs2.get("date_modified"));
        assertEquals(2, actualObs2.get("program_id"));
        assertEquals("test program 2", actualObs2.get("program_name"));
    }
}
