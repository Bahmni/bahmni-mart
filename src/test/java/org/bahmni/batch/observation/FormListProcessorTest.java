package org.bahmni.batch.observation;

import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FormListProcessorTest {

	private List<Concept> addMoreConcepts;

	private List<Concept> allConcepts;

	private List<Concept> historyAndExaminationConcepts;

	private List<Concept> vitalsConcepts;

	private List<Concept> operationNotesConcepts;

	@Mock
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Before
	public void setup(){
		initMocks(this);

		addMoreConcepts = new ArrayList<>();
		addMoreConcepts.add(new Concept(3365,"Operation Notes Template",1));
		addMoreConcepts.add(new Concept(1200,"Discharge Summary, Surgeries and Procedures",1));
		addMoreConcepts.add(new Concept(1206,"Other Notes",0));

		allConcepts = new ArrayList<>();
		allConcepts.add(new Concept(1189,"History and Examination",1));
		allConcepts.add(new Concept(56	,"Vitals",1));
		allConcepts.add(new Concept(3365,"Operation Notes Template",1));

		historyAndExaminationConcepts = new ArrayList<>();
		historyAndExaminationConcepts.add(new Concept(1190,"Chief Complaint Data",	1));
		historyAndExaminationConcepts.add(new Concept(1194,"Chief Complaint Notes",	0));
		historyAndExaminationConcepts.add(new Concept(1843,"History",	0));
		historyAndExaminationConcepts.add(new Concept(1844,"Examination",	0));
		historyAndExaminationConcepts.add(new Concept(2077,"Image",	0));

		vitalsConcepts = new ArrayList<>();
		vitalsConcepts.add(new Concept(1842,"Vitals Notes",0));

		operationNotesConcepts = new ArrayList<>();
		operationNotesConcepts.add(new Concept(3351,"Anesthesia Administered",0));
		operationNotesConcepts.add(new Concept(1206,"Other Notes",0));

	}

	@Test
	@Ignore
	public void shouldRetrieveAllForms(){
		FormListProcessor formListProcessor = new FormListProcessor();
		formListProcessor.setConceptListSqlResource(new ByteArrayResource("blah..blah..blah".getBytes()));
		formListProcessor.setConceptDetailsSqlResource(new ByteArrayResource("getAddMoreConceptDetails".getBytes()));
		formListProcessor.setJdbcTemplate(jdbcTemplate);
		formListProcessor.setAddMoreConceptNames("\"Operation Notes Template\",\"Discharge Summary, Surgeries and Procedures\",\"Other Notes\"");
		formListProcessor.postConstruct();

		when(jdbcTemplate.query(eq("blah..blah..blah"),any(MapSqlParameterSource.class), Matchers.<BeanPropertyRowMapper<Concept>>any()))
				.thenReturn(allConcepts)
				.thenReturn(historyAndExaminationConcepts)
				.thenReturn(new ArrayList<Concept>())
				.thenReturn(vitalsConcepts)
				.thenReturn(operationNotesConcepts)
				.thenReturn(new ArrayList<Concept>());

		when(jdbcTemplate.query(eq("getAddMoreConceptDetails"),any(MapSqlParameterSource.class),Matchers.<BeanPropertyRowMapper<Concept>>any())).thenReturn(addMoreConcepts);

		List<Form> forms = formListProcessor.retrieveFormList();

		assertEquals(5,forms.size());
		assertEquals("History and Examination", forms.get(0).getFormName().getName());
		assertEquals("Chief Complaint Notes", forms.get(0).getFields().get(0).getName());
		assertEquals("History", forms.get(0).getFields().get(1).getName());
		assertEquals("Examination", forms.get(0).getFields().get(2).getName());
		assertEquals("Image", forms.get(0).getFields().get(3).getName());

		assertEquals("Vitals", forms.get(1).getFormName().getName());
		assertEquals(vitalsConcepts, forms.get(1).getFields());

		assertEquals("Operation Notes Template", forms.get(2).getFormName().getName());
		assertEquals("Anesthesia Administered", forms.get(2).getFields().get(0).getName());
		assertEquals("Other Notes", forms.get(2).getIgnoredFields().get(0).getName());

		assertEquals("Discharge Summary, Surgeries and Procedures", forms.get(3).getFormName().getName());
		assertEquals(0, forms.get(3).getFields().size());
		assertEquals(0, forms.get(3).getIgnoredFields().size());

		assertEquals("Other Notes", forms.get(4).getFormName().getName());
		assertEquals(0, forms.get(4).getFields().size());
		assertEquals(0, forms.get(4).getIgnoredFields().size());

	}

}
