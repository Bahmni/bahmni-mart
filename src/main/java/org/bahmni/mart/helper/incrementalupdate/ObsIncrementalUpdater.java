package org.bahmni.mart.helper.incrementalupdate;

import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObsIncrementalUpdater extends AbstractIncrementalUpdater {
    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Override
    protected boolean getMetaDataChangeStatus(String actualTableName) {
        TableData currentTableData = formTableMetadataGenerator.getTableDataByName(actualTableName);
        TableData existingTableData = getExistingTableData(actualTableName);
        return !currentTableData.equals(existingTableData);
    }
}
