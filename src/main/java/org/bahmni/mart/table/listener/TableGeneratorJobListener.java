package org.bahmni.mart.table.listener;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQL;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;

@Component
@Scope(value = "prototype")
public class TableGeneratorJobListener extends AbstractJobListener {

    private static final Logger logger = LoggerFactory.getLogger(TableGeneratorJobListener.class);

    private static final String LIMIT = " LIMIT 1";

    private CodesProcessor codesProcessor;
    private TableData tableData;

    private static void setTableColumnType(TableColumn tableColumn) {
        tableColumn.setType(Constants.getPostgresDataTypeFor(tableColumn.getType()));
    }

    @Override
    protected void setUpPreProcessorData() {
        if (codesProcessor != null)
            codesProcessor.setUpCodesData();
    }

    @Override
    public TableData getTableDataForMart(String jobName) {

        if (tableData != null) {
            return tableData;
        }
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);

        tableData = getTableData(jobDefinition, getSql(jobDefinition));

        return tableData;
    }

    private String getSql(JobDefinition jobDefinition) {
        String sql = getReaderSQLByIgnoringColumns(jobDefinition.getColumnsToIgnore(),
                getReaderSQL(jobDefinition));

        if (StringUtils.isEmpty(sql)) {
            throw new InvalidJobConfiguration(String
                    .format("Reader SQL is empty for the job definition '%s'", jobDefinition.getName()));
        }
        return sql;
    }

    private TableData getTableData(JobDefinition jobDefinition, String sql) {
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        TableData tableData = openMRSJdbcTemplate.query(sql + LIMIT, resultSetExtractor);
        tableData.setName(jobDefinition.getTableName());
        tableData.getColumns().forEach(TableGeneratorJobListener::setTableColumnType);

        return tableData;
    }

    @Override
    protected void logError(Exception e) {
        logger.error(e.getMessage(), e);
    }

    public void setCodesProcessor(CodesProcessor codesProcessor) {
        this.codesProcessor = codesProcessor;
    }
}
