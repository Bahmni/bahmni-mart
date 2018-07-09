package org.bahmni.mart.helper.incrementalupdate;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQL;


@Component
public class CustomSqlIncrementalUpdater extends AbstractIncrementalUpdater {

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    protected boolean getMetaDataChangeStatus(String jobName) {
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByProcessedName(jobName);
        if (isEmpty(jobDefinition.getName()) || isNull(jobDefinition.getIncrementalUpdateConfig()))
            return true;
        String tableName = jobDefinition.getTableName();

        TableData existingTableData = getExistingTableData(tableName);
        TableData tableData = tableDataGenerator.getTableData(tableName, getReaderSQL(jobDefinition));


        return !tableData.equals(existingTableData);
    }
}
