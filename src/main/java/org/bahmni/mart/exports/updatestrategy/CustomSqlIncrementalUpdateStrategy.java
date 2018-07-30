package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.SpecialCharacterResolver;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.AbstractJobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;


@Component
public class CustomSqlIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    public boolean getMetaDataChangeStatus(String tableName, String jobName) {
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        if (isEmpty(jobDefinition.getName()) || isNull(jobDefinition.getIncrementalUpdateConfig()))
            return true;

        TableData tableData = listener.getTableDataForMart(jobDefinition);
        SpecialCharacterResolver.resolveTableData(tableData);
        return !tableData.equals(getExistingTableData(tableName));
    }

    @Override
    public void setListener(AbstractJobListener listener) {
        this.listener = listener;
    }
}
