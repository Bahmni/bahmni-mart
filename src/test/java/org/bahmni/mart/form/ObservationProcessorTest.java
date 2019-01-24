package org.bahmni.mart.form;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ObservationProcessorTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private FormFieldTransformer formFieldTransformer;

    @Mock
    private JobDefinition jobDefinition;

    private List<Obs> obsList;

    private BahmniForm form;

    private ObservationProcessor observationProcessor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        form = new BahmniForm();
        form.setFormName(new Concept(1, "formName", 0));

        observationProcessor = new ObservationProcessor();
        setValuesForMemberFields(observationProcessor, "jdbcTemplate", namedParameterJdbcTemplate);
        setValuesForMemberFields(observationProcessor, "obsDetailSqlResource",
                new ByteArrayResource("blah..blah..blah".getBytes()));
        setValuesForMemberFields(observationProcessor, "leafObsSqlResource",
                new ByteArrayResource("get..all..child".getBytes()));
        setValuesForMemberFields(observationProcessor, "formObsSqlResource",
                new ByteArrayResource("get..some..child".getBytes()));
        setValuesForMemberFields(observationProcessor, "formFieldTransformer", formFieldTransformer);
        setValuesForMemberFields(observationProcessor, "jobDefinition", jobDefinition);

        obsList = new ArrayList<>();
        obsList.add(new Obs(1, null, new Concept(1, "systolic", 0), "120"));
        obsList.add(new Obs(1, null, new Concept(1, "diastolic", 0), "80"));
        obsList.add(new Obs(1, null, new Concept(1, "abcd", 0), "180"));
        observationProcessor.postConstruct();
    }

    @Test
    public void shouldRetrieveAllChildObsIds() throws Exception {
        observationProcessor.setForm(form);
        observationProcessor.postConstruct();

        Map<String, Object> firstLevelObs1 = new HashMap<>();
        firstLevelObs1.put("isSet", true);
        firstLevelObs1.put("obsId", 2);
        Map<String, Object> firstLevelObs2 = new HashMap<>();
        firstLevelObs2.put("isSet", false);
        firstLevelObs2.put("obsId", 3);

        List<Map<String, Object>> firstLevelObs = new ArrayList<>();
        firstLevelObs.add(firstLevelObs1);
        firstLevelObs.add(firstLevelObs2);

        Map<String, Object> secondLevelObs1 = new HashMap<>();
        secondLevelObs1.put("isSet", true);
        secondLevelObs1.put("obsId", 2);
        Map<String, Object> secondLevelObs2 = new HashMap<>();
        secondLevelObs2.put("isSet", false);
        secondLevelObs2.put("obsId", 3);

        List<Map<String, Object>> secondLevelObs = new ArrayList<>();
        secondLevelObs.add(secondLevelObs1);
        secondLevelObs.add(secondLevelObs2);


        Map<String, Object> thirdLevelObs1 = new HashMap<>();
        thirdLevelObs1.put("isSet", false);
        thirdLevelObs1.put("obsId", 5);

        List<Map<String, Object>> thirdLevelObs = new ArrayList<>();
        thirdLevelObs.add(thirdLevelObs1);


        when(namedParameterJdbcTemplate.query(eq("blah..blah..blah"), any(Map.class), any(ColumnMapRowMapper.class)))
                .thenReturn(firstLevelObs)
                .thenReturn(secondLevelObs)
                .thenReturn(thirdLevelObs);

        List<Integer> allChildObsGroupIds = new ArrayList<>();
        observationProcessor.retrieveChildObsIds(allChildObsGroupIds, Arrays.asList(1));
        assertEquals(3, allChildObsGroupIds.size());
    }

    @Test
    public void shouldReturnObsListOfAllLeafObs() {

        Concept systolicConcept = new Concept(1, "systolic", 0);
        Concept diastolicConcept = new Concept(2, "diastolic", 0);
        Concept abcdConcept = new Concept(3, "abcd", 0);
        form.addField(systolicConcept);
        form.addField(diastolicConcept);
        obsList = new ArrayList<>();
        obsList.add(new Obs(1, 0, systolicConcept, "120"));
        obsList.add(new Obs(2, 0, diastolicConcept, "80"));
        obsList.add(new Obs(3, 0, abcdConcept, "180"));

        when(formFieldTransformer.transformFormToFieldIds(form)).thenReturn(Arrays.asList(1, 2));


        observationProcessor.setForm(form);
        observationProcessor.postConstruct();

        when(namedParameterJdbcTemplate.query(eq("blah..blah..blah"), any(Map.class), any(SingleColumnRowMapper.class)))
                .thenReturn(Arrays.asList(1, 2, 3))
                .thenReturn(new ArrayList());

        when(namedParameterJdbcTemplate.query(eq("get..all..child"), any(Map.class), any(BeanPropertyRowMapper.class)))
                .thenReturn(obsList);

        Map<String, Object> map = new HashMap<>();
        map.put("parent_obs_id", new Integer(1));
        map.put("obs_id", new Integer(0));

        List<Obs> obsListActual = observationProcessor.process(map);
        assertEquals(3, obsListActual.size());
        assertEquals(new Integer(1), obsListActual.get(0).getParentId());
        assertEquals(new Integer(1), obsListActual.get(1).getParentId());
        assertEquals(new Integer(1), obsListActual.get(2).getParentId());
    }

    @Test
    public void shouldReturnFormObsOnlyIfItIsSet() {

        Concept systolicConcept = new Concept(1, "systolic", 0);
        Concept diastolicConcept = new Concept(2, "diastolic", 1);

        form.addField(systolicConcept);
        form.addField(diastolicConcept);
        obsList = new ArrayList<>();
        obsList.add(new Obs(1, 0, systolicConcept, "120"));
        obsList.add(new Obs(2, 0, diastolicConcept, "80"));

        when(formFieldTransformer.transformFormToFieldIds(form)).thenReturn(Arrays.asList(1, 2));

        observationProcessor.setForm(form);
        observationProcessor.postConstruct();

        when(namedParameterJdbcTemplate.query(eq("get..some..child"), any(Map.class), any(BeanPropertyRowMapper.class)))
                .thenReturn(obsList);

        Map<String, Object> map = new HashMap<>();
        map.put("parent_obs_id", new Integer(1));
        map.put("obs_id", new Integer(0));

        List<Obs> obsListActual = observationProcessor.process(map);
        assertEquals(1, obsListActual.size());
        assertEquals(new Integer(1), obsListActual.get(0).getParentId());
    }


    @Test
    public void shouldReturnEmptyListWhenChildObsAndFieldIdsAreEmpty() throws Exception {
        Concept systolicConcept = new Concept(1, "systolic", 0);
        form.addField(systolicConcept);
        observationProcessor.setForm(form);
        Map<String, Object> obsRow = new HashMap<>();
        obsRow.put("parent_obs_id", new Integer(1));
        List<Integer> fieldIds = new ArrayList<>();
        when(formFieldTransformer.transformFormToFieldIds(form)).thenReturn(fieldIds);
        form.getFormName().setIsSet(1);

        assertTrue(observationProcessor.process(obsRow).isEmpty());
    }

    @Test
    public void shouldAddCodeFromJobDefinitionAndParentObsIdToParamsMap() throws Exception {
        ArgumentCaptor<HashMap> captor = ArgumentCaptor.forClass(HashMap.class);
        Concept testConcept = new Concept(1, "test", 0);
        form.addField(testConcept);
        observationProcessor.setForm(form);
        Map<String, Object> obsRow = new HashMap<>();
        obsRow.put("obs_id", 1);
        obsRow.put("parent_obs_id",2);
        List<Integer> fieldIds = new ArrayList<>();
        fieldIds.add(1);
        when(formFieldTransformer.transformFormToFieldIds(form)).thenReturn(fieldIds);

        when(jobDefinition.getType()).thenReturn("obs");
        String locale = "test locale";
        when(jobDefinition.getLocale()).thenReturn(locale);
        String conceptReferenceSource = "BAHMNI-INTERNAL";
        when(jobDefinition.getConceptReferenceSource()).thenReturn(conceptReferenceSource);

        observationProcessor.process(obsRow);

        verify(namedParameterJdbcTemplate, times(1)).query(eq("get..all..child"),
                captor.capture(), any(BeanPropertyRowMapper.class));
        HashMap childrenParams = captor.getAllValues().get(0);
        assertEquals(conceptReferenceSource, childrenParams.get("conceptReferenceSource"));
        assertEquals("[1]", childrenParams.get("leafConceptIds").toString());
        assertEquals("[1]", childrenParams.get("childObsIds").toString());
        assertEquals("2", childrenParams.get("parentObsId").toString());
        assertEquals(conceptReferenceSource, childrenParams.get("conceptReferenceSource"));
        assertEquals(locale, childrenParams.get("locale"));

        verify(namedParameterJdbcTemplate, times(1)).query(eq("get..some..child"),
                captor.capture(), any(BeanPropertyRowMapper.class));
        HashMap childParams = captor.getAllValues().get(1);
        assertEquals(conceptReferenceSource, childParams.get("conceptReferenceSource"));
        assertEquals(1, childParams.get("obsId"));
        assertEquals(2, childParams.get("parentObsId"));
        assertEquals(locale, childParams.get("locale"));
    }
}
