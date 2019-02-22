package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.bahmni.mart.table.SpecialCharacterResolver.getActualTableName;

@Component
@Qualifier("obsIncrementalStrategy")
public class ObsIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {
    @Autowired
    private TableMetadataGenerator tableMetadataGenerator;

    @Override
    public boolean getMetaDataChangeStatus(String processedName, String jobName) {
        TableData currentTableData = tableMetadataGenerator.getTableDataByName(getActualTableName(processedName));
        SpecialCharacterResolver.resolveTableData(currentTableData);

        TableData existingTableData = getExistingTableData(processedName);
        return !currentTableData.equals(existingTableData);
    }

    public void setTableMetadataGenerator(TableMetadataGenerator tableMetadataGenerator) {
        this.tableMetadataGenerator = tableMetadataGenerator;
    }
}
