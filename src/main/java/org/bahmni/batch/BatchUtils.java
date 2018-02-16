package org.bahmni.batch;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bahmni.batch.exception.BatchResourceException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BatchUtils {

    public static String convertResourceOutputToString(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new BatchResourceException("Cannot load the provided resource. Unable to continue", e);
        }
    }

    public static List<String> convertConceptNamesToSet(String conceptNames) {
        if (StringUtils.isEmpty(conceptNames))
            return new ArrayList<>();
        String[] tokens = conceptNames.split("\"(\\s*),(\\s*)\"");
        return Arrays.stream(tokens).map(token -> token.replaceAll("\"", "")).collect(Collectors.toList());
    }

}
