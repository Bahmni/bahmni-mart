package org.bahmni.mart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.exception.BatchResourceException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class BatchUtils {

    public static int stepNumber = 0;

    public static String convertResourceOutputToString(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new BatchResourceException("Cannot load the provided resource. Unable to continue", e);
        }
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
          case "BIT":
              if ("true".equalsIgnoreCase(value)) {
                  return "B'1'";
              } else {
                  return "B'0'";
              }
          default:
              return value;
        }
    }

    public static String constructSqlWithParameter(String sql, String parameter, String value) {
        String processedValue = escapeApostropheInString(value);
        return sql.replaceAll(String.format(":%s", parameter), String.format("'%s'", processedValue));
    }

    public static String constructSqlWithParameter(String sql, String parameter, boolean value) {
        return sql.replaceAll(String.format(":%s", parameter), String.format("%s", value));
    }

    public static String constructSqlWithParameter(String sql, String parameter, List<String> values) {
        String joinedValues = values.stream()
                .map(value -> String.format("'%s'", escapeApostropheInString(value)))
                .collect(Collectors.joining(", "));
        return sql.replaceAll(String.format(":%s", parameter), String.format("%s", joinedValues));
    }

    private static String getStringForPsql(String value) {
        return String.format("'%s'", value);
    }

    private static String escapeApostropheInString(String value) {
        String escapeApostrophe = "\\\\\\\\'";
        return StringUtils.isEmpty(value) ? value : value.replaceAll("'", escapeApostrophe);
    }
}

