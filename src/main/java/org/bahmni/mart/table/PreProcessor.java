package org.bahmni.mart.table;

import java.util.Map;

public interface PreProcessor {

    Map<String, Object> process(Map<String, Object> item);

}