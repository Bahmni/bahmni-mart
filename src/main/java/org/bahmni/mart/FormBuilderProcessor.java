package org.bahmni.mart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FormBuilderProcessor {

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    JdbcTemplate openmrsDbTemplate;

    public List<BahmniForm> getAllForms() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        List<Map<String, Object>> forms = openmrsDbTemplate.queryForList("SELECT form.form_id, form.name, form.version, f.value_reference FROM form INNER JOIN form_resource f ON form.form_id = f.form_id WHERE form.retired = FALSE AND form.published = TRUE ");
        for (Map<String, Object> form : forms) {
            String name = (String) form.get("name");
            String valueReference = (String) form.get("value_reference");
            String version = (String) form.get("version");
            Integer formId = (Integer) form.get("form_id");
            BahmniForm bahmniForm = new BahmniForm();
            bahmniForm.setFormName(new Concept(formId, String.format("%s.%s/", name, version), 0));

            addHierarchy(bahmniForm, valueReference);

            bahmniForms.add(bahmniForm);
        }
        return bahmniForms;
    }

    private void addHierarchy(BahmniForm form, String valueReference) {
        try {
            JsonArray controls = new JsonParser().parse(new FileReader("/Users/sumanm/amman/bahmni-mart/Spike.json")).getAsJsonObject().get("controls").getAsJsonArray();

            addControls(form, controls);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void addControls(BahmniForm form, JsonArray controls) {
        for (JsonElement control : controls) {
            JsonObject controlAsJsonObject = control.getAsJsonObject();
            String type = controlAsJsonObject.get("type").getAsString();
            if (type.equals("obsControl")){
                form.addField(getConcept(control.getAsJsonObject().get("concept").getAsJsonObject()));
            }
        }
    }

    private Concept getConcept(JsonObject jsonObject) {
        String conceptName = jsonObject.get("name").getAsString();
        String datatype = jsonObject.get("datatype").getAsString();
        Concept concept = new Concept();
        concept.setName(conceptName);
        concept.setDataType(datatype);
        concept.setId(1206);
        return concept;
    }

//    private BahmniForm convertToBahmniForm(JsonObject control) {
//        JsonObject concept = control.get("concept").getAsJsonObject();
//        Concept conceptInfo = new Concept(0, concept.get("name").getAsString(), 0);
//
//
//
//        BahmniForm bahmniForm = new BahmniForm();
//        bahmniForm.setFormName(conceptInfo);
//        return null;
//    }


}
