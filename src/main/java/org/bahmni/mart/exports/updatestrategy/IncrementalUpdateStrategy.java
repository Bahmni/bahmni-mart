package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.table.domain.TableData;

import java.util.Set;

public interface IncrementalUpdateStrategy {

    boolean isMetaDataChanged(String keyName);

    String updateReaderSql(String readerSql, String jobName, String updateOn);

    void deleteVoidedRecords(Set<String> ids, String tableName, String columnName);

    void updateMarker(String jobName);

    TableData getExistingTableData(String actualTableName);
}
