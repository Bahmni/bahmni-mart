package org.bahmni.batch.observation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bahmni.batch.form.domain.BahmniForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class DynamicObsQuery {

	private static final Logger log = LoggerFactory.getLogger(DynamicObsQuery.class);

	@Autowired
	private Configuration configuration;

	public String getSqlQueryForForm(BahmniForm form){

		StringWriter stringWriter = new StringWriter();
		try {
			Template obsSqlTemplate = configuration.getTemplate("obsWithParentSql.ftl");

			Map<String, Object> patientData = new HashMap<>();
			patientData.put("form", form);

			obsSqlTemplate.process(patientData, stringWriter);


		}catch(Exception ex){
			throw new RuntimeException("Unable to continue generating a sql",ex);
		}
		String sql = stringWriter.toString();
		log.debug("The sql generated for the form ["+form.getFormName().getName()+"] is ");
		log.debug(sql);
		return sql;
	}


}
