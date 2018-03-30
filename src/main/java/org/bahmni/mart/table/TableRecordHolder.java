package org.bahmni.mart.table;

import java.util.List;
import java.util.Map;


public class TableRecordHolder {

    private List<Map<String, Object>> recordList;
    private String tableName;

    public TableRecordHolder(List<Map<String, Object>> recordList, String tableName) {
        this.recordList = recordList;
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Map<String, Object>> recordList) {
        this.recordList = recordList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
