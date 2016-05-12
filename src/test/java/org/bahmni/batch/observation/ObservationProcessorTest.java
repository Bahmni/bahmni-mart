package org.bahmni.batch.observation;

import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Obs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ObservationProcessorTest {

	@Mock
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Mock
	private FormFieldTransformer formFieldTransformer;

	private List<Obs> obsList;

	@Before
	public void setup(){
		initMocks(this);

		obsList = new ArrayList<>();
		obsList.add(new Obs("treatment1",1,null,new Concept(1,"systolic",0),"120"));
		obsList.add(new Obs("treatment1",1,null,new Concept(1,"diastolic",0),"80"));
		obsList.add(new Obs("treatment1",1,null,new Concept(1,"abcd",0),"180"));

	}

	@Test
	public void shouldRetrieveAllChildObsIds() throws Exception {
		ObservationProcessor observationProcessor = new ObservationProcessor();
		observationProcessor.setJdbcTemplate(namedParameterJdbcTemplate);
		observationProcessor.setObsDetailSqlResource(new ByteArrayResource("blah..blah..blah".getBytes()));
		observationProcessor.setForm(null);
		observationProcessor.postConstruct();

		when(namedParameterJdbcTemplate.query(eq("blah..blah..blah"),any(Map.class),any(SingleColumnRowMapper.class)))
				.thenReturn(Arrays.asList(2,3,4))
				.thenReturn(Arrays.asList(5,6,7))
				.thenReturn(new ArrayList());

		List<Integer> allChildObsGroupIds = new ArrayList<>();
		observationProcessor.retrieveChildObsIds(allChildObsGroupIds,Arrays.asList(1));
		assertEquals(7, allChildObsGroupIds.size());
	}

	@Test
	public void shouldReturnObsListOfAllLeafObs() throws Exception {
		ObservationProcessor observationProcessor = new ObservationProcessor();
		observationProcessor.setJdbcTemplate(namedParameterJdbcTemplate);
		observationProcessor.setObsDetailSqlResource(new ByteArrayResource("blah..blah..blah".getBytes()));
		observationProcessor.setLeafObsSqlResource(new ByteArrayResource("get..all..child".getBytes()));
		observationProcessor.setFormFieldTransformer(formFieldTransformer);
		observationProcessor.setForm(null);
		observationProcessor.postConstruct();

		when(namedParameterJdbcTemplate.query(eq("blah..blah..blah"),any(Map.class),any(SingleColumnRowMapper.class)))
				.thenReturn(Arrays.asList(2,3,4))
				.thenReturn(Arrays.asList(5,6,7))
				.thenReturn(new ArrayList());

		when(namedParameterJdbcTemplate.query(eq("get..all..child"),any(Map.class),any(BeanPropertyRowMapper.class)))
				.thenReturn(obsList);

		Map<String, Object> map = new HashMap<>();
		map.put("obsGroupId",new Integer(1));
		map.put("obsId",new Integer(0));

		List<Obs> obsListActual = observationProcessor.process(map);
		assertEquals(3, obsListActual.size());
		assertEquals(new Integer(1), obsListActual.get(0).getParentId());
		assertEquals(new Integer(1), obsListActual.get(1).getParentId());
		assertEquals(new Integer(1), obsListActual.get(2).getParentId());
	}

}
