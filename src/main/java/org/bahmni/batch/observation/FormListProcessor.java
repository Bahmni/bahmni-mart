package org.bahmni.batch.observation;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class FormListProcessor {

	private JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/conceptList.sql")
	private Resource conceptListSqlResource;

	private String conceptListSql = null;

	public static final String ALL_FORMS = "All Observation Templates";

	List<Concept> addMoreConcepts;

	public FormListProcessor(List<Concept> addMoreConcepts){
		this.addMoreConcepts = addMoreConcepts;
	}

	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Form> retrieveFormList(){
		List<Concept> allFormConcepts = getConcepts(ALL_FORMS);
		for(Concept concept: addMoreConcepts){
			if(!allFormConcepts.contains(concept)){
				allFormConcepts.add(concept);
			}
		}

		return getFormsFromConceptList(allFormConcepts);
	}

	private List<Form> getFormsFromConceptList(List<Concept> concepts){
		List<Form> forms = new ArrayList<>();

		for(Concept concept: concepts){
			Form form = new Form();
			form.setFormName(concept);
			retrieveFormFields(form,concept);
			forms.add(form);
		}
		return forms;
	}

	private void retrieveFormFields(Form form, Concept concept) {
		List<Concept> childConcepts = getConcepts(concept.getName());

		for(Concept childConcept: childConcepts){
			addFieldsToForm(form,childConcept);
		}
	}

	private void addFieldsToForm(Form form, Concept concept){
		if(addMoreConcepts.contains(concept)){
			form.addIgnoreField(concept);
			return;
		}

		if(concept.getSet() == 0){
			form.addField(concept);
			return;
		}

		retrieveFormFields(form,concept);
	}

	private List<Concept> getConcepts(String conceptName){
		return jdbcTemplate.query(conceptListSql,new Object[]{ conceptName }, new BeanPropertyRowMapper<Concept>());
	}

	@PostConstruct
	public void postConstruct(){
		this.conceptListSql = BatchUtils.convertResourceOutputToString(conceptListSqlResource);
	}


	public void setConceptListSqlResource(Resource conceptListSqlResource) {
		this.conceptListSqlResource = conceptListSqlResource;
	}
}
