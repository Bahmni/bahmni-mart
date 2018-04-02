package org.bahmni.mart;

import org.apache.commons.io.IOUtils;
import org.bahmni.mart.exception.BatchResourceException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

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
          default:
              return value;
        }
    }

    public static String constructSqlWithParameter(String sql, String parameter, String value) {
        return sql.replaceAll(String.format(":%s", parameter), String.format("'%s'", value));
    }

    private static String getStringForPsql(String value) {
        return String.format("'%s'", value);
    }
}

