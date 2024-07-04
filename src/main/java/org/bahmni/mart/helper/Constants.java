package org.bahmni.mart.helper;

import java.util.HashMap;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.startsWith;

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
            put("double", "double precision");
            put("int", "integer");
            put("float8", "double precision");
            put("bigint", "integer");
            put("mediumtext", "text");
        }
    };

    public static String getPostgresDataTypeFor(String key) {
        if (startsWith(lowerCase(key), "int"))
            key = "int";
        return postgresDataTypeMap.getOrDefault((key != null) ? lowerCase(key) : null, key);
    }

}
