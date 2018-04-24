package org.bahmni.mart.helper;

import com.google.gson.JsonElement;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractConfigParserHelper {

    protected Set<Map.Entry<String, JsonElement>> getAllConceptSet(String implementationConfigFile,
                                                                   String defaultConfigFile) {
        return Stream
                .concat(getConceptSet(implementationConfigFile).stream(), getConceptSet(defaultConfigFile).stream())
                .filter(distinctByKey(Map.Entry::getKey)).collect(toSet());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> keys = ConcurrentHashMap.newKeySet();
        return key -> keys.add(keyExtractor.apply(key));
    }

    protected abstract Set<Map.Entry<String, JsonElement>> getConceptSet(String filePath);
}
