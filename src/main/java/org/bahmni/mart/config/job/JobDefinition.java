package org.bahmni.mart.config.job;

import java.util.List;

public class JobDefinition {
    private String name;
    private String type;
    private String readerSql;
    private int chunkSizeToRead;
    private String tableName;
    private List<String> separateTables;
    private String conceptReferenceSource;
    private List<String> columnsToIgnore;
    private EavAttributes eavAttributes;

    public List<String> getSeparateTables() {
        return separateTables;
    }

    public void setSeparateTables(List<String> separateTables) {
        this.separateTables = separateTables;
    }

    public int getChunkSizeToRead() {
        return chunkSizeToRead;
    }

    public void setChunkSizeToRead(int chunkSizeToRead) {
        this.chunkSizeToRead = chunkSizeToRead;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReaderSql() {
        return readerSql;
    }

    public void setReaderSql(String readerSql) {
        this.readerSql = readerSql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getConceptReferenceSource() {
        return conceptReferenceSource;
    }

    public List<String> getColumnsToIgnore() {
        return columnsToIgnore;
    }

    public EavAttributes getEavAttributes() {
        return eavAttributes;
    }

    public void setEavAttributes(EavAttributes eavAttributes) {
        this.eavAttributes = eavAttributes;
    }

}
