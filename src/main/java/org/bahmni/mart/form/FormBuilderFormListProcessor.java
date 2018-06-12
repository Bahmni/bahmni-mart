package org.bahmni.mart.form;

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
public class FormBuilderFormListProcessor {

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    JdbcTemplate openmrsDbTemplate;

    public List<BahmniForm> getAllForms() {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        List<Map<String, Object>> forms = openmrsDbTemplate.queryForList("SELECT form.form_id, form.name," +
                " form.version, f.value_reference FROM form INNER JOIN form_resource f ON form.form_id = f.form_id " +
                "WHERE form.retired = FALSE AND form.published = TRUE ");
        for (Map<String, Object> form : forms) {
            String name = (String) form.get("name");
            String valueReference = (String) form.get("value_reference");
            String version = (String) form.get("version");
            Integer formId = (Integer) form.get("form_id");
            BahmniForm bahmniForm = new BahmniForm();
            Concept formName = new Concept(null, String.format("%s.%s/", name, version), 1);
            bahmniForm.setFormName(formName);

            addHierarchy(bahmniForm, valueReference);

            bahmniForms.add(bahmniForm);
        }
        return bahmniForms;
    }

    private void addHierarchy(BahmniForm form, String valueReference) {
//        String fileName = String.format("/Users/sumanm/amman/bahmni-mart/%s.json", form.getFormName().getName().replace(".", "_").replace("/", ""));
        String fileName = valueReference;
        try {
            JsonArray controls = new JsonParser().parse(new FileReader(fileName)).getAsJsonObject().get("controls").getAsJsonArray();

            addControls(form, controls);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addControls(BahmniForm form, JsonArray controls) {
        for (JsonElement control : controls) {
            JsonObject controlAsJsonObject = control.getAsJsonObject();
            String type = controlAsJsonObject.get("type").getAsString();

            switch (type) {
                case "obsControl": {
                    JsonObject jsonConcept = control.getAsJsonObject().get("concept").getAsJsonObject();

                    form.addField(getConcept(jsonConcept));
                    break;
                }
                case "obsGroupControl": {
                    JsonObject jsonConcept = control.getAsJsonObject().get("concept").getAsJsonObject();

                    ArrayList<Concept> concepts = getConcepts(jsonConcept);
                    for (Concept concept : concepts) {
                        form.addField(concept);
                    }
                    break;
                }
                case "section":
                    addControls(form, control.getAsJsonObject().getAsJsonArray("controls"));
                    break;
            }
        }
    }

    private ArrayList<Concept> getConcepts(JsonObject jsonConcept) {
        return getConcepts(jsonConcept, new ArrayList<>());
    }

    private ArrayList<Concept> getConcepts(JsonObject jsonConcept, ArrayList<Concept> concepts) {
        if (jsonConcept.get("setMembers") == null)
            return concepts;
        for (JsonElement setMember : jsonConcept.getAsJsonArray("setMembers")) {
            JsonObject asJsonObject = setMember.getAsJsonObject();
            if (asJsonObject.get("setMembers") == null)
                concepts.add(getConcept(setMember.getAsJsonObject()));
            else
                getConcepts(asJsonObject, concepts);
        }
        return concepts;
    }

    private Concept getConcept(JsonObject jsonObject) {
        String conceptName = jsonObject.get("name").getAsString();
        String datatype = jsonObject.get("datatype").getAsString();
        Concept concept = new Concept();
        concept.setName(conceptName);
        concept.setDataType(datatype);
        concept.setId(getConceptId(jsonObject.get("uuid").getAsString()));
        concept.setUuid(jsonObject.get("uuid").getAsString());
        return concept;
    }

    private Integer getConceptId(String uuid) {
        return (Integer) openmrsDbTemplate.queryForList(String.format("SELECT concept_id FROM concept WHERE uuid = '%s'", uuid)).get(0).get("concept_id");
    }
}
