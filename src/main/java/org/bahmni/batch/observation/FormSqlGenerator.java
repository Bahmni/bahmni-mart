package org.bahmni.batch.observation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.bahmni.batch.form.domain.BahmniForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class FormSqlGenerator {

	@Autowired
	private Configuration configuration;

	public String generateSqlForStep(BahmniForm form){

		try {
			Template obsSqlTemplate = configuration.getTemplate("obsWithParentSql.ftl","UTF-8");

			Map<String, Object> patientData = new HashMap<>();
			patientData.put("form", form);

			StringWriter stringWriter = new StringWriter();
			obsSqlTemplate.process(patientData, stringWriter);

			return stringWriter.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		throw new RuntimeException("Unable to continue generating a sql");
	}


}
