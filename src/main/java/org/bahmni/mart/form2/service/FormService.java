package org.bahmni.mart.form2.service;

import org.bahmni.mart.BatchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormService {

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    JdbcTemplate openmrsDbTemplate;

    @Value("classpath:sql/form2FormList.sql")
    private Resource form2FormListResource;

    public Map<String, String> getAllLatestFormPaths() {
        Map<String, String> formPaths = new HashMap<>();
        final String form2FormListQuery = BatchUtils.convertResourceOutputToString(form2FormListResource);
        List<Map<String, Object>> forms = openmrsDbTemplate.queryForList(form2FormListQuery);
        for (Map<String, Object> form : forms) {
            String name = (String) form.get("name");
            String valueReference = (String) form.get("value_reference");
            formPaths.put(name, valueReference);
        }
        return formPaths;
    }
}
