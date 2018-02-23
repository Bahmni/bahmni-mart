package org.bahmni.analytics.helper;

import java.util.HashMap;

public class Constants {

    private static HashMap<String, String> postgresDataTypeMap = new HashMap<String, String>() {
        {
            put("Datetime", "date");
            put("Boolean", "text");
            put("Numeric", "numeric");
            put("Time", "time");
            put("Date", "date");
            put("Text", "text");
            put("N/A", "integer");
            put("Coded", "text");
        }
    };

    public static String getPostgresDataTypeFor(String key) {
        return postgresDataTypeMap.get(key);
    }

}
