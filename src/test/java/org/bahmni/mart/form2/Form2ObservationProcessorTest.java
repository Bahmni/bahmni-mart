package org.bahmni.mart.form2;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class Form2ObservationProcessorTest {

    private Form2ObservationProcessor form2ObservationProcessor;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        form2ObservationProcessor = new Form2ObservationProcessor();
        setValuesForMemberFields(form2ObservationProcessor, "jdbcTemplate", jdbcTemplate);
    }

    @Test
    public void shouldReturnObsWithParentFormFieldPathAndReferenceFormFieldPathWhenParentFormDepthToParentIs1() {
        BahmniForm form = mock(BahmniForm.class);
        BahmniForm rootForm = new BahmniForm();
        rootForm.setFormName(new Concept(1, "FormOne", 0));
        when(form.getRootForm()).thenReturn(rootForm);
        BahmniForm parentForm = mock(BahmniForm.class);
        when(parentForm.getDepthToParent()).thenReturn(1);
        when(parentForm.getDepthToParent()).thenReturn(1);
        when(form.getParent()).thenReturn(parentForm);
        when(form.getDepthToParent()).thenReturn(3);
        when(jobDefinition.getConceptReferenceSource()).thenReturn(null);
        form2ObservationProcessor.setForm(form);
        form2ObservationProcessor.setJobDefinition(jobDefinition);
        Map<String, Object> encounterIdMap = new HashMap<>();
        encounterIdMap.put("encounter_id", "abc123");
        when(jdbcTemplate.query(any(), any(Map.class), any(BeanPropertyRowMapper.class)))
                .thenReturn(Collections.singletonList(getObsRow()));

        List<Obs> obsList = form2ObservationProcessor.process(encounterIdMap);

        assertEquals(1, obsList.size());
        Obs obs = obsList.get(0);
        assertEquals("FormOne.1/3-0/7-0/2-1", obs.getFormFieldPath());
        assertEquals("FormOne.1/3-0", obs.getReferenceFormFieldPath());
    }

    private Obs getObsRow() {
        Obs obsRow = new Obs();
        obsRow.setEncounterId("abc123");
        obsRow.setPatientId("def789");
        obsRow.setField(new Concept(7,"age", 0));
        obsRow.setId(97);
        obsRow.setValue("21");
        obsRow.setObsDateTime("2015-02-01 00:00:00");
        obsRow.setDateCreated("2016-11-09 7:31:43");
        obsRow.setLocationId("8");
        obsRow.setLocationName("Registration");
        obsRow.setProgramId("1");
        obsRow.setProgramName("First Stage Validation");
        obsRow.setFormFieldPath("FormOne.1/3-0/7-0/2-1");
        obsRow.setReferenceFormFieldPath("FormOne.1/3-0");
        return obsRow;
    }
}
