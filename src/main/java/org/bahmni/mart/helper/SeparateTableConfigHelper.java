package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bahmni.mart.BatchUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class SeparateTableConfigHelper {
    @Value("${defaultMultiSelectAndAddMore}")
    private String defaultConfigFile;

    @Value("${implementationMultiSelectAndAddMore}")
    private String implementationConfigFile;

    @Value("${ignoreConcepts}")
    private String ignoreConcepts;

    private static final String ALLOW_ADD_MORE_KEY = "allowAddMore";
    private static final String MULTI_SELECT_KEY = "multiSelect";
    private static final String CONCEPT_SET_UI_KEY = "conceptSetUI";
    private static final String CONFIG_KEY = "config";

    public List<String> getConceptNames() {
        List<String> multiSelectAndAddMore = new ArrayList<>();
        List<String> ignoreConceptsList = BatchUtils.convertConceptNamesToSet(ignoreConcepts);

        for (Map.Entry<String, JsonElement> concept : getAllConceptSet()) {
            String conceptName = concept.getKey();
            JsonObject conceptConfig = concept.getValue().getAsJsonObject();

            if (isAddMoreOrMultiSelect(conceptConfig.get(ALLOW_ADD_MORE_KEY), conceptConfig.get(MULTI_SELECT_KEY)) &&
                    !ignoreConceptsList.contains(conceptName)) {
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
                .concat(getConceptSet(defaultConfigFile).stream(), getConceptSet(implementationConfigFile).stream())
                .collect(toSet());
    }

    private Set<Map.Entry<String, JsonElement>> getConceptSet(String multiSelectAndAddMoreFile) {
        try {
            JsonObject jsonConfig = (JsonObject) new JsonParser().parse(new FileReader(multiSelectAndAddMoreFile));

            return jsonConfig.getAsJsonObject(CONFIG_KEY).getAsJsonObject(CONCEPT_SET_UI_KEY).entrySet();
        } catch (FileNotFoundException e) {
            return Collections.emptySet();
        }
    }
}
