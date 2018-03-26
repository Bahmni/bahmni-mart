package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class TableGeneratorJobListener extends AbstractJobListener {

    private static final Logger logger = LoggerFactory.getLogger(TableGeneratorJobListener.class);

    public static final String LIMIT = " LIMIT 1";

    @Override
    public TableData getTableDataForMart(String jobName) {
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        String readerSQLAfterIgnoringColumns = JobDefinitionUtil
                .getReaderSQLByIgnoringColumns(jobDefinition.getColumnsToIgnore(), jobDefinition.getReaderSql());
        if (readerSQLAfterIgnoringColumns == null || readerSQLAfterIgnoringColumns.isEmpty()) {
            throw new InvalidJobConfiguration(String
                    .format("Reader SQL is empty for the job definition '%s'", jobName));
        }
        TableData tableData = openMRSJdbcTemplate.query(readerSQLAfterIgnoringColumns + LIMIT, resultSetExtractor);
        tableData.setName(jobDefinition.getTableName());
        tableData.getColumns().forEach(tableColumn -> tableColumn
                .setType(Constants.getPostgresDataTypeFor(tableColumn.getType())));
        return tableData;
    }

    @Override
    protected void logError(Exception e) {
        logger.error(e.getMessage(), e);
    }
}
