package org.bahmni.batch.observation;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.form.BahmniFormFactory;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.ObsService;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Component
public class FormListProcessor {

	private static final Logger log = LoggerFactory.getLogger(FormListProcessor.class);

	@Autowired
	private ObsService obsService;

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

	@Value("${headerConceptSource}")
	private String headerConceptSource;

	@Autowired
	private BahmniFormFactory bahmniFormFactory;

	@Autowired
	public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public List<BahmniForm> retrieveForms(){
		List<Concept> allFormConcepts = obsService.getChildConcepts(ALL_FORMS);
		List<BahmniForm> forms = new ArrayList<>();
		for(Concept concept: allFormConcepts){
			forms.add(bahmniFormFactory.createForm(concept,null));
		}
		fetchExportFormsList(forms, new ArrayList<BahmniForm>());
		return forms;
	}

	public List<BahmniForm> fetchExportFormsList(List<BahmniForm> forms, List<BahmniForm> flattenedList){

		for(BahmniForm form : forms){
			if(form.getChildren().size()!=0) {
				flattenedList.addAll(form.getChildren());
				fetchExportFormsList(form.getChildren(), flattenedList);
			}
		}

		return flattenedList;
	}

}
