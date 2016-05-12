package org.bahmni.batch.observation;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class FormListProcessor {

	private static final Logger log = LoggerFactory.getLogger(FormListProcessor.class);

	private NamedParameterJdbcTemplate jdbcTemplate;

	@Value("classpath:sql/conceptList.sql")
	private Resource conceptListSqlResource;

	@Value("classpath:sql/conceptDetails.sql")
	private Resource conceptDetailsSqlResource;

	private String conceptListSql;

	private String conceptDetailsSql;

	public static final String ALL_FORMS = "All Observation Templates";

	@Value("${addMoreConcepts}")
	private String addMoreConceptNames;


	//
//	@Value("${addMoreConceptsSiteSpecific}")
//	private String addMoreConceptNamesSiteSpecific;

	@Autowired
	public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Form> retrieveFormList(){

		List<Concept> addMoreConcepts = getAddMoreConceptDetails();
		List<Concept> allFormConcepts = getConcepts(ALL_FORMS);

		mergeLists(addMoreConcepts, allFormConcepts);

		return getFormsFromConceptList(allFormConcepts,addMoreConcepts);
	}

	private void mergeLists(List<Concept> addMoreConcepts, List<Concept> allFormConcepts) {
		for(Concept concept: addMoreConcepts){
			if(!allFormConcepts.contains(concept)){
				allFormConcepts.add(concept);
			}
		}
	}

	private List<Concept> getAddMoreConceptDetails() {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("conceptNames", BatchUtils.convertConceptNamesToSet(addMoreConceptNames));

		return jdbcTemplate.query(conceptDetailsSql,parameters,new BeanPropertyRowMapper<>(Concept.class));
	}


	private List<Form> getFormsFromConceptList(List<Concept> concepts, List<Concept> addMoreConcepts){
		List<Form> forms = new ArrayList<>();

		for(Concept concept: concepts){
			Form form = new Form();
			form.setFormName(concept);
			retrieveFormFields(form,concept,addMoreConcepts);
			forms.add(form);
		}
		return forms;
	}

	private void retrieveFormFields(Form form, Concept concept, List<Concept> addMoreConcepts) {
		List<Concept> childConcepts = getConcepts(concept.getName());

		for(Concept childConcept: childConcepts){
			addFieldsToForm(form,childConcept,addMoreConcepts);
		}
	}

	private void addFieldsToForm(Form form, Concept concept, List<Concept> addMoreConcepts){
		if(addMoreConcepts.contains(concept)){
			form.addIgnoreField(concept);
			return;
		}

		if(concept.getIsSet() == 0){
			form.addField(concept);
			return;
		}

		retrieveFormFields(form,concept,addMoreConcepts);
	}

	private List<Concept> getConcepts(String conceptName){
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("parentConceptName",conceptName);

		return jdbcTemplate.query(conceptListSql, parameters, new BeanPropertyRowMapper<>(Concept.class));
	}

	@PostConstruct
	public void postConstruct(){
		this.conceptListSql = BatchUtils.convertResourceOutputToString(conceptListSqlResource);
		this.conceptDetailsSql = BatchUtils.convertResourceOutputToString(conceptDetailsSqlResource);
		log.debug("Printing the list of concepts with addMore configured in properties");
		log.debug(addMoreConceptNames);

	}


	public void setConceptListSqlResource(Resource conceptListSqlResource) {
		this.conceptListSqlResource = conceptListSqlResource;
	}

	public void setConceptDetailsSqlResource(Resource conceptDetailsSqlResource) {
		this.conceptDetailsSqlResource = conceptDetailsSqlResource;
	}

	public void setAddMoreConceptNames(String addMoreConceptNames) {
		this.addMoreConceptNames = addMoreConceptNames;
	}

}
