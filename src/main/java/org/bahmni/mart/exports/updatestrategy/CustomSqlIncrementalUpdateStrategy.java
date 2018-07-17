package org.bahmni.mart.exports.updatestrategy;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQL;


@Component
public class CustomSqlIncrementalUpdateStrategy extends AbstractIncrementalUpdateStrategy {

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    public boolean getMetaDataChangeStatus(String tableName, String jobName) {
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        if (isEmpty(jobDefinition.getName()) || isNull(jobDefinition.getIncrementalUpdateConfig()))
            return true;
        try {
            TableData tableData = tableDataGenerator.getTableData(tableName, getReaderSQL(jobDefinition));
            return !tableData.equals(getExistingTableData(tableName));
        } catch (BadSqlGrammarException exception) {
            return false;
        }
    }
}
