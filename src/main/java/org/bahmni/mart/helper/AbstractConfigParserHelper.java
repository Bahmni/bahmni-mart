package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.isNull;

public abstract class AbstractConfigParserHelper {

    protected Set<Map.Entry<String, JsonElement>> getAllConcepts(String implementationConfigFile,
                                                                 String defaultConfigFile) {
        return mergeJsonObjects(getConceptSet(implementationConfigFile), getConceptSet(defaultConfigFile));
    }

    private static Set<Map.Entry<String, JsonElement>> mergeJsonObjects(JsonObject jsonObject1,
                                                                        JsonObject jsonObject2) {
        Set<String> keys = getKeys(jsonObject1, jsonObject2);

        HashMap<String, JsonElement> stringJsonElementHashMap = new HashMap<>();

        for (String key : keys) {
            stringJsonElementHashMap.put(key, merge((JsonObject) jsonObject1.get(key),
                    (JsonObject) jsonObject2.get(key)));
        }

        return stringJsonElementHashMap.entrySet();
    }

    private static JsonObject merge(JsonObject firstPriority, JsonObject secondPriority) {
        if (isNull(firstPriority))
            return secondPriority;
        if (isNull(secondPriority))
            return firstPriority;

        JsonObject jsonObject = new JsonObject();
        for (String key : getKeys(firstPriority, secondPriority))
            jsonObject.add(key, getValue(firstPriority.get(key), secondPriority.get(key)));

        return jsonObject;
    }

    private static Set<String> getKeys(JsonObject object1, JsonObject object2) {
        Set<String> keys = new HashSet<>(object1.keySet());
        keys.addAll(new HashSet<>(object2.keySet()));
        return keys;
    }

    private static JsonElement getValue(JsonElement firstPriority, JsonElement secondPriority) {
        return isNull(firstPriority) ? secondPriority : firstPriority;
    }

    protected abstract JsonObject getConceptSet(String filePath);
}
