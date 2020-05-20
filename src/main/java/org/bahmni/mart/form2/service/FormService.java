package org.bahmni.mart.form2.service;

import com.google.gson.Gson;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.form2.model.FormNameJsonMetadata;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

@Component
public class FormService {

    private static final String FORM_NAME = "name";
    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    JdbcTemplate openmrsDbTemplate;
    @Value("classpath:sql/form2FormList.sql")
    private Resource form2FormListResource;

    public Map<String, String> getAllLatestFormPaths() {
        Map<String, String> formPaths = new HashMap<>();
        List<Map<String, Object>> forms = executeFormListQuery();
        for (Map<String, Object> form : forms) {
            String name = (String) form.get(FORM_NAME);
            String valueReference = (String) form.get("value_reference");
            formPaths.put(name, valueReference);
        }
        return formPaths;
    }

    private List<Map<String, Object>> executeFormListQuery() {
        final String form2FormListQuery = BatchUtils.convertResourceOutputToString(form2FormListResource);
        return openmrsDbTemplate.queryForList(form2FormListQuery);
    }

    public Map<String, Integer> getFormNamesWithLatestVersionNumber() {
        LinkedHashMap<String, Integer> formNameAndVersionMap = new LinkedHashMap<>();
        List<Map<String, Object>> forms = getLatestFormNamesWithVersion();
        forms.forEach(form -> {
            String name = (String) form.get(FORM_NAME);
            int version = Integer.parseInt((String) form.get("version"));
            formNameAndVersionMap.put(name, version);
        });
        return formNameAndVersionMap;
    }

    private List<Map<String, Object>> getLatestFormNamesWithVersion() {
        return openmrsDbTemplate.queryForList("SELECT name , MAX(version) as version FROM form GROUP BY name");
    }

    public String getTranslated(String formName) {
        LinkedHashMap<String, String> formNameAndTranslationMap = new LinkedHashMap<>();
        List<Map<String, Object>> forms = getFormNameTranslation();
        for (Map<String, Object> form : forms) {
            String name = (String) form.get(FORM_NAME);
            String value_reference = (String) form.get("value_reference");
            Gson gson = new Gson();
            List<FormNameJsonMetadata> list = Arrays.asList(gson.fromJson(value_reference, FormNameJsonMetadata[].class));

            String translation = list.stream().filter(nam -> (nam.getLocale().equals("fr")))
                    .map(FormNameJsonMetadata::getDisplay).findFirst().toString();
             translation = translation != null ? translation :  list.stream().filter(nam -> (nam.getLocale().equals("en")))
                    .map(FormNameJsonMetadata::getDisplay).findFirst().toString();
        formNameAndTranslationMap.put(name, translation);
        }

        return formNameAndTranslationMap.get(formName) != null ? formNameAndTranslationMap.get(formName) : formName;
    }

    private List<Map<String, Object>> getFormNameTranslation() {
        return openmrsDbTemplate.queryForList("select f.name, fr.value_reference from form f, form_resource fr where fr.form_id = f.form_id and fr.name like '%FormName_Translation'");
    }
}
