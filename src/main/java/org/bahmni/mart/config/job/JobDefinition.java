package org.bahmni.mart.config.job;

import java.util.List;

public class JobDefinition {
    private String name;
    private String type;
    private String readerSql;
    private int chunkSizeToRead;
    private String tableName;
    private SeparateTableConfig separateTableConfig;
    private String conceptReferenceSource;
    private List<String> columnsToIgnore;
    private EavAttributes eavAttributes;
    private String sourceFilePath;
    private boolean ignoreAllFreeTextConcepts;
    private List<CodeConfig> codeConfigs;
    private List<GroupedJobConfig> groupedJobConfigs;


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

    public void setColumnsToIgnore(List<String> columnsToIgnore) {
        this.columnsToIgnore = columnsToIgnore;
    }

    public EavAttributes getEavAttributes() {
        return eavAttributes;
    }

    public boolean getIgnoreAllFreeTextConcepts() {
        return ignoreAllFreeTextConcepts;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public List<CodeConfig> getCodeConfigs() {
        return codeConfigs;
    }

    public void setCodeConfigs(List<CodeConfig> codeConfigs) {
        this.codeConfigs = codeConfigs;
    }

    public SeparateTableConfig getSeparateTableConfig() {
        return separateTableConfig;
    }

    public List<GroupedJobConfig> getGroupedJobConfigs() {
        return groupedJobConfigs;
    }
}
