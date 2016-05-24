package org.bahmni.batch.helper;

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
public class FreeMarkerEvaluator<T> {

	private static final Logger log = LoggerFactory.getLogger(FreeMarkerEvaluator.class);

	@Autowired
	private Configuration configuration;

	public String evaluate(String templateName, T input){

		StringWriter stringWriter = new StringWriter();
		try {
			Template template = configuration.getTemplate(templateName);

			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("input", input);

			template.process(inputMap, stringWriter);


		}catch(Exception ex){
			throw new RuntimeException("Unable to continue generating a the template with name ["+templateName+"]",ex);
		}

		String result = stringWriter.toString();
		log.debug("The generated template for ["+input.toString()+"] is ");
		log.debug(result);

		return result;
	}

}
