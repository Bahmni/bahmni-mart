package com.bahmni.batch.bahmnianalytics.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TableRecordHolder {

    private List<Map<String, Object>> recordList = new ArrayList<>();
    private String tableName;

    public TableRecordHolder(List<Map<String, Object>> recordList, String tableName) {
        this.recordList = recordList;
        this.tableName = tableName;
    }

    public TableRecordHolder() {
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
