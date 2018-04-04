package org.bahmni.mart.helper;

import java.util.HashMap;

public class Constants {

    private static HashMap<String, String> postgresDataTypeMap = new HashMap<String, String>() {
        {
            put("datetime", "timestamp");
            put("boolean", "text");
            put("numeric", "numeric");
            put("time", "time");
            put("date", "date");
            put("text", "text");
            put("n/a", "integer");
            put("coded", "text");
            put("tinyint", "boolean");
            put("char", "text");
            put("varchar", "text");
            put("complex", "text");
            put("double","double precision");
        }
    };

    public static String getPostgresDataTypeFor(String key) {
        return postgresDataTypeMap.getOrDefault((key != null) ? key.toLowerCase() : null, key);
    }

}
