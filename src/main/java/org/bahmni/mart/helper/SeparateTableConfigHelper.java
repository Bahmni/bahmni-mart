package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bahmni.mart.config.job.JobDefinition;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getSeparateTableNamesForJob;

@Component
public class SeparateTableConfigHelper {

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

    @PostConstruct
    private void separateTableConfigHelperPostConstruct() {
        defaultAddMoreAndMultiSelectConceptsNames = getAddMoreAndMultiSelectConceptNames();
    }

    public List<String> getAddMoreAndMultiSelectConceptNames() {
        List<String> multiSelectAndAddMore = new ArrayList<>();
        for (Map.Entry<String, JsonElement> concept : getAllConceptSet()) {
            String conceptName = concept.getKey();
            JsonObject conceptConfig = concept.getValue().getAsJsonObject();
            if (isAddMoreOrMultiSelect(conceptConfig.get(ALLOW_ADD_MORE_KEY), conceptConfig.get(MULTI_SELECT_KEY))) {
                multiSelectAndAddMore.add(conceptName);
            }
        }
        return multiSelectAndAddMore;
    }

    private boolean isAddMoreOrMultiSelect(JsonElement allowAddMore, JsonElement multiSelect) {
        return getBoolean(allowAddMore) || getBoolean(multiSelect);
    }

    private boolean getBoolean(JsonElement value) {
        return value != null && value.getAsBoolean();
    }

    private Set<Map.Entry<String, JsonElement>> getAllConceptSet() {
        return Stream
                .concat(getConceptSet(implementationConfigFile).stream(), getConceptSet(defaultConfigFile).stream())
                .filter(distinctByKey(Map.Entry::getKey)).collect(toSet());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> keys = ConcurrentHashMap.newKeySet();
        return key -> keys.add(keyExtractor.apply(key));
    }

    private Set<Map.Entry<String, JsonElement>> getConceptSet(String multiSelectAndAddMoreFile) {
        try {
            JsonObject jsonConfig = (JsonObject) new JsonParser().parse(new FileReader(multiSelectAndAddMoreFile));
            JsonObject configKeyJson = jsonConfig.getAsJsonObject(CONFIG_KEY);
            return configKeyJson != null && configKeyJson.getAsJsonObject(CONCEPT_SET_UI_KEY) != null ?
                    configKeyJson.getAsJsonObject(CONCEPT_SET_UI_KEY).entrySet() : Collections.emptySet();
        } catch (FileNotFoundException | ClassCastException e) {
            log.warn(e.getMessage());
            return Collections.emptySet();
        }
    }

    public HashSet<Concept> getSeparateTableConceptsForJob(JobDefinition jobDefinition) {
        HashSet<Concept> separateTableConcepts = new HashSet<>();
        List<String> separateTableConceptNames = getSeparateTableNamesForJob(jobDefinition);
        separateTableConceptNames.addAll(defaultAddMoreAndMultiSelectConceptsNames);
        if (!separateTableConceptNames.isEmpty()) {
            separateTableConcepts.addAll(conceptService.getConceptsByNames(separateTableConceptNames));
        }
        return separateTableConcepts;
    }
}
