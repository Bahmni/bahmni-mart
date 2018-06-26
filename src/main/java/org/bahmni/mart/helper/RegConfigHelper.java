package org.bahmni.mart.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RegConfigHelper extends AbstractConfigParserHelper {
    private static final Logger log = LoggerFactory.getLogger(RegConfigHelper.class);
    private static final String REG_EXTENSION_POINT_ID = "org.bahmni.registration.conceptSetGroup.observations";
    private static final String REG_TYPE = "config";

    @Value("${defaultExtensionConfigPath}")
    private String defaultExtensionConfigFile;

    @Value("${implementationExtensionConfigPath}")
    private String implementationExtensionConfigFile;

    @Override
    protected JsonObject getConceptSet(String filePath) {
        try {
            return (JsonObject) new JsonParser().parse(new FileReader(filePath));
        } catch (FileNotFoundException | ClassCastException e) {
            log.warn(e.getMessage(), e);
            return new JsonObject();
        }
    }


    public List<String> getRegConcepts() {
        ArrayList<String> regConcepts = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> allConcepts = getAllConcepts(implementationExtensionConfigFile,
                defaultExtensionConfigFile);
        for (Map.Entry<String, JsonElement> concept : allConcepts) {
            JsonObject jsonObject = concept.getValue().getAsJsonObject();
            JsonElement extensionPointId = jsonObject.get("extensionPointId");
            JsonElement regType = jsonObject.get("type");
            JsonElement extensionParams = jsonObject.get("extensionParams");
            if (extensionPointId != null && regType != null && extensionParams != null &&
                    REG_EXTENSION_POINT_ID.equals(extensionPointId.getAsString()) &&
                    REG_TYPE.equals(regType.getAsString()))
                regConcepts.add(extensionParams.getAsJsonObject().get("conceptName").getAsString());
        }
        return regConcepts;
    }

}
