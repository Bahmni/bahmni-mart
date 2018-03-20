package org.bahmni.mart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bahmni.mart.exception.BatchResourceException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BatchUtils {

    public static int stepNumber = 0;

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

    public static String getPostgresCompatibleValue(String value, String dataType) {
        if (value == null)
            return null;

        switch (dataType) {
          case "text":
              return getStringForPsql(value.replaceAll("'", "''"));
          case "date":
          case "timestamp":
          case "time":
              return getStringForPsql(value);
          default:
              return value;
        }
    }

    public static String constructSqlWithParameter(String sql, String parameter, String value) {
        return sql.replaceAll(":" + parameter, "'" +
                value + "'");
    }

    private static String getStringForPsql(String value) {
        return String.format("'%s'", value);
    }
}

