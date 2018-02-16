package org.bahmni.batch.helper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bahmni.batch.exception.BatchResourceException;
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

    public String evaluate(String templateName, T input) {
        StringWriter stringWriter = new StringWriter();
        try {
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("input", input);
            template.process(inputMap, stringWriter);
        } catch (Exception exception) {
            throw new BatchResourceException(String.format("Unable to continue generating a the template with name [%s]", templateName), exception);
        }
        String result = stringWriter.toString();
        log.debug(String.format("The generated template for [%s]", input.toString()));
        log.debug(result);

        return result;
    }

}
