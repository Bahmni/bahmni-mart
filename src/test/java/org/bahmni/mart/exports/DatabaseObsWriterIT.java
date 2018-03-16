package org.bahmni.mart.exports;

import org.bahmni.mart.AbstractBaseBatchIT;
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

public class DatabaseObsWriterIT extends AbstractBaseBatchIT {

    @Autowired
    private DatabaseObsWriter databaseObsWriter;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Test
    public void shouldWriteObsToDB() throws Exception {
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
        Obs obs2 = new Obs(2, 2, fieldTwo, "test IT");
        obs2.setEncounterId("56");
        obs2.setPatientId("23");

        obsList1.add(obs1);
        obsList1.add(obs2);

        ArrayList<Obs> obsList2 = new ArrayList<>();
        Obs obs3 = new Obs(3, 2, fieldOne, "40");
        obs3.setEncounterId("560");
        obs3.setPatientId("3");
        Obs obs4 = new Obs(4, 2, fieldTwo, "test IT 2");
        obs4.setEncounterId("560");
        obs4.setPatientId("3");

        obsList2.add(obs3);
        obsList2.add(obs4);

        items.add(obsList1);
        items.add(obsList2);

        formTableMetadataGenerator.addMetadataForForm(bahmniForm);
        databaseObsWriter.setForm(bahmniForm);
        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList());

        databaseObsWriter.write(items);

        List<Map<String, Object>> maps = martJdbcTemplate.queryForList("SELECT * FROM \"test\"");
        assertEquals(2, maps.size());

        Map<String, Object> actualObs1 = maps.get(0);
        assertEquals(5, actualObs1.size());
        assertEquals(1, actualObs1.get("id_test"));
        assertEquals(23, actualObs1.get("patient_id"));
        assertEquals(56, actualObs1.get("encounter_id"));
        assertEquals(new BigDecimal(4), actualObs1.get("field_one"));
        assertEquals("test IT", actualObs1.get("field_two"));

        Map<String, Object> actualObs2 = maps.get(1);
        assertEquals(5, actualObs2.size());
        assertEquals(3, actualObs2.get("id_test"));
        assertEquals(3, actualObs2.get("patient_id"));
        assertEquals(560, actualObs2.get("encounter_id"));
        assertEquals(new BigDecimal(40), actualObs2.get("field_one"));
        assertEquals("test IT 2", actualObs2.get("field_two"));
    }
}