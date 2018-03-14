package org.bahmni.mart.helper;

import java.util.HashMap;

public class Constants {

    private static HashMap<String, String> postgresDataTypeMap = new HashMap<String, String>() {
        {
            put("datetime", "date");
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
        }
    };

    public static String getPostgresDataTypeFor(String key) {
        String keyInLowerCase = (key != null) ? key.toLowerCase() : null;
        return postgresDataTypeMap.getOrDefault(keyInLowerCase, key);
    }

}
