package org.bahmni.mart;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.Collections;

import static java.util.stream.Collectors.toSet;

@Component
public class ConfigMultiSelectAndAddMore {
    @Value("${defaultMultiSelectAndAddMore}")
    private String defaultMultiSelectAndAddMore;

    @Value("${implementationMultiSelectAndAddMore}")
    private String implementationMultiSelectAndAddMore;

    @Value("${ignoreConcepts}")
    private String ignoreConcepts;

    private static final String ALLOW_ADD_MORE = "allowAddMore";
    private static final String MULTI_SELECT = "multiSelect";

    private List<String> multiSelectAndAddMore = new ArrayList<>();

    public List<String> getMultiSelectAndAddMore() {
        Set<Map.Entry<String, JsonElement>> conceptSet = getAllConceptSet();
        List<String> ignoreConceptsList = BatchUtils.convertConceptNamesToSet(ignoreConcepts);

        for (Map.Entry<String, JsonElement> key : conceptSet) {
            String conceptName = key.getKey();
            JsonObject value = key.getValue().getAsJsonObject();
            JsonElement allowAddMore = value.get(ALLOW_ADD_MORE);
            JsonElement multiSelect = value.get(MULTI_SELECT);
            boolean isAddMore = allowAddMore != null && allowAddMore.getAsBoolean();
            boolean isMultiSelect = multiSelect != null && multiSelect.getAsBoolean();

            if ((isAddMore || isMultiSelect) && !ignoreConceptsList.contains(conceptName)) {
                multiSelectAndAddMore.add(conceptName);
            }
        }
        return multiSelectAndAddMore;
    }

    private Set<Map.Entry<String, JsonElement>> getAllConceptSet() {
        Set<Map.Entry<String, JsonElement>> defaultConceptSet = getConceptSet(defaultMultiSelectAndAddMore);
        Set<Map.Entry<String, JsonElement>> implementationConceptSet =
                getConceptSet(implementationMultiSelectAndAddMore);
        return Stream
                .concat(defaultConceptSet.stream(),implementationConceptSet.stream())
                .collect(toSet());
    }

    private Set<Map.Entry<String, JsonElement>> getConceptSet(String multiSelectAndAddMoreFile) {
        try {
            FileReader json = new FileReader(multiSelectAndAddMoreFile);
            JsonElement parse = new JsonParser().parse(json);
            JsonObject asJsonObject = parse.getAsJsonObject();
            JsonObject configSet = asJsonObject.getAsJsonObject("config").getAsJsonObject("conceptSetUI");
            return configSet.entrySet();
        } catch (FileNotFoundException e) {
            return Collections.emptySet();
        }
    }
}
