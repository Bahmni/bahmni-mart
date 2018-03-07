package org.bahmni.mart.table;

import org.bahmni.mart.table.domain.TableData;

import java.util.List;

public interface TableMetadataGenerator {
    List<TableData> getTables();
}
