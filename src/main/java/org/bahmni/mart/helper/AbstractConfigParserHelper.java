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
        JsonObject defaultConceptSet = getConceptSet(defaultConfigFile);
        if (shouldOverrideConfig(defaultConfigFile))
            return mergeJsonObjects(getConceptSet(implementationConfigFile), defaultConceptSet);
        return mergeJsonObjects(new JsonObject(), defaultConceptSet);
    }

    protected abstract boolean shouldOverrideConfig(String defaultConfigFile);

    protected boolean shouldOverrideConfig(JsonObject jsonObject) {
        JsonElement shouldOverRideConfig = jsonObject.get("shouldOverRideConfig");
        return !isNull(shouldOverRideConfig) && shouldOverRideConfig.getAsBoolean();
    }

    private static Set<Map.Entry<String, JsonElement>> mergeJsonObjects(JsonObject jsonObject1,
                                                                        JsonObject jsonObject2) {

        return getEntries(jsonObject1, jsonObject2, getKeys(jsonObject1, jsonObject2));
    }

    private static Set<Map.Entry<String, JsonElement>> getEntries(JsonObject jsonObject1, JsonObject jsonObject2,
                                                                  Set<String> keys) {
        HashMap<String, JsonElement> stringJsonElementHashMap = new HashMap<>();

        for (String key : keys) {
            if (instanceOfJsonObject(jsonObject1.get(key)) && instanceOfJsonObject(jsonObject2.get(key)))
                stringJsonElementHashMap.put(key, merge((JsonObject) jsonObject1.get(key),
                        (JsonObject) jsonObject2.get(key)));
        }

        return stringJsonElementHashMap.entrySet();
    }

    private static boolean instanceOfJsonObject(JsonElement jsonElement) {
        return isNull(jsonElement) || jsonElement instanceof JsonObject;
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
