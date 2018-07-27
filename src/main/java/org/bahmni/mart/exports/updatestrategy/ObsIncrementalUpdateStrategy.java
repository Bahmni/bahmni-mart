package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.bahmni.mart.table.SpecialCharacterResolver.getActualTableName;

@Component
public class ObsIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {
    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Override
    public boolean getMetaDataChangeStatus(String processedName, String jobName) {
        TableData currentTableData = formTableMetadataGenerator.getTableDataByName(getActualTableName(processedName));
        SpecialCharacterResolver.resolveTableData(currentTableData);

        TableData existingTableData = getExistingTableData(processedName);
        return !currentTableData.equals(existingTableData);
    }
}
