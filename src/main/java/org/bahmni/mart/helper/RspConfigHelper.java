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
public class RspConfigHelper extends AbstractConfigParserHelper {
    private static final Logger log = LoggerFactory.getLogger(RspConfigHelper.class);
    private static final String RSP_EXTENSION_POINT_ID = "org.bahmni.registration.conceptSetGroup.observations";
    private static final String RSP_TYPE = "config";

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


    public List<String> getRspConcepts() {
        ArrayList<String> rspConcepts = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> allConcepts = getAllConcepts(implementationExtensionConfigFile,
                defaultExtensionConfigFile);
        for (Map.Entry<String, JsonElement> concept : allConcepts) {
            JsonObject jsonObject = concept.getValue().getAsJsonObject();
            JsonElement extensionPointId = jsonObject.get("extensionPointId");
            JsonElement rspType = jsonObject.get("type");
            JsonElement extensionParams = jsonObject.get("extensionParams");
            if (extensionPointId != null && rspType != null && extensionParams != null &&
                    RSP_EXTENSION_POINT_ID.equals(extensionPointId.getAsString()) &&
                    RSP_TYPE.equals(rspType.getAsString()))
                rspConcepts.add(extensionParams.getAsJsonObject().get("conceptName").getAsString());
        }
        return rspConcepts;
    }

}
