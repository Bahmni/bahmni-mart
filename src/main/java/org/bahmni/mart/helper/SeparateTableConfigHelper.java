package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.SeparateTableConfig;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getSeparateTableNamesForJob;

@Component
public class SeparateTableConfigHelper extends AbstractConfigParserHelper {

    private static final Logger log = LoggerFactory.getLogger(SeparateTableConfigHelper.class);
    private static final String ALLOW_ADD_MORE_KEY = "allowAddMore";
    private static final String MULTI_SELECT_KEY = "multiSelect";
    private static final String CONCEPT_SET_UI_KEY = "conceptSetUI";
    private static final String CONFIG_KEY = "config";

    @Value("${defaultConfigPath}")
    private String defaultConfigFile;

    @Value("${implementationConfigPath}")
    private String implementationConfigFile;

    @Autowired
    private ConceptService conceptService;

    private List<String> defaultAddMoreAndMultiSelectConceptsNames;

    private List<String> defaultAddMoreConceptNames = new ArrayList<>();

    @PostConstruct
    private void separateTableConfigHelperPostConstruct() {
        defaultAddMoreAndMultiSelectConceptsNames = getAddMoreAndMultiSelectConceptNames();
        setAddMoreConceptNames();
    }

    public List<String> getAddMoreAndMultiSelectConceptNames() {
        List<String> multiSelectAndAddMore = new ArrayList<>();
        for (Map.Entry<String, JsonElement> concept : getAllConcepts(implementationConfigFile, defaultConfigFile)) {
            String conceptName = concept.getKey();
            JsonObject conceptConfig = concept.getValue().getAsJsonObject();
            if (isAddMoreOrMultiSelect(conceptConfig.get(ALLOW_ADD_MORE_KEY), conceptConfig.get(MULTI_SELECT_KEY))) {
                multiSelectAndAddMore.add(conceptName);
            }
        }
        return multiSelectAndAddMore;
    }

    public boolean isAddMore(String conceptName) {
        return defaultAddMoreConceptNames.contains(conceptName);
    }

    private void setAddMoreConceptNames() {
        for (Map.Entry<String, JsonElement> concept : getAllConceptSet()) {
            JsonObject conceptConfig = concept.getValue().getAsJsonObject();
            if (getBoolean(conceptConfig.get(ALLOW_ADD_MORE_KEY))) {
                defaultAddMoreConceptNames.add(concept.getKey());
            }
        }
    }

    private boolean isAddMoreOrMultiSelect(JsonElement allowAddMore, JsonElement multiSelect) {
        return getBoolean(allowAddMore) || getBoolean(multiSelect);
    }

    public boolean isAddMoreOrMultiSelect(Concept concept) {
        return defaultAddMoreAndMultiSelectConceptsNames.contains(concept.getName());
    }

    private boolean getBoolean(JsonElement value) {
        return value != null && value.getAsBoolean();
    }

    @Override
    protected JsonObject getConceptSet(String filePath) {
        try {
            JsonObject jsonConfig = (JsonObject) new JsonParser().parse(new FileReader(filePath));
            JsonObject configKeyJson = jsonConfig.getAsJsonObject(CONFIG_KEY);
            return configKeyJson != null && configKeyJson.getAsJsonObject(CONCEPT_SET_UI_KEY) != null ?
                    configKeyJson.getAsJsonObject(CONCEPT_SET_UI_KEY) : new JsonObject();
        } catch (FileNotFoundException | ClassCastException e) {
            log.warn(e.getMessage());
            return new JsonObject();
        }
    }

    public HashSet<Concept> getSeparateTableConceptsForJob(JobDefinition jobDefinition) {
        HashSet<Concept> separateTableConcepts = new HashSet<>();
        List<String> separateTableConceptNames = getSeparateTableNamesForJob(jobDefinition);
        SeparateTableConfig separateTableConfig = jobDefinition.getSeparateTableConfig();
        if (separateTableConfig != null && separateTableConfig.isEnableForAddMoreAndMultiSelect())
            separateTableConceptNames.addAll(defaultAddMoreAndMultiSelectConceptsNames);
        if (!separateTableConceptNames.isEmpty()) {
            separateTableConcepts.addAll(conceptService.getConceptsByNames(separateTableConceptNames));
        }
        return separateTableConcepts;
    }
}
