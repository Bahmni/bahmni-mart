package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class EavIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {

    @Autowired
    private EAVJobListener eavJobListener;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    protected boolean getMetaDataChangeStatus(String actualTableName, String jobName) {
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        if (isEmpty(jobDefinition.getName()) || isNull(jobDefinition.getIncrementalUpdateConfig()))
            return true;

        TableData tableData = eavJobListener.getTableDataForMart(jobDefinition.getName());

        return !tableData.equals(getExistingTableData(jobDefinition.getTableName()));
    }
}
