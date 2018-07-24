package org.bahmni.mart.helper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.bahmni.mart.exception.BatchResourceException;
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
        return getString(templateName, input, false);
    }

    public String evaluate(String s, T input, boolean voided) {
        return getString(s, input, voided);
    }

    private String getString(String templateName, T input, Boolean voided) {
        StringWriter stringWriter = new StringWriter();
        try {
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("input", input);
            inputMap.put("voided", voided);
            template.process(inputMap, stringWriter);
        } catch (Exception exception) {
            throw new BatchResourceException(
                    String.format("Unable to continue generating a the template with name [%s]", templateName),
                    exception);
        }
        String result = stringWriter.toString();
        log.debug(String.format("The generated template for [%s]", input.toString()));
        return result;
    }
}
