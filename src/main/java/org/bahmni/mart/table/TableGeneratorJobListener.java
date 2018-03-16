package org.bahmni.mart.table;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.helper.Constants;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TableGeneratorJobListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(TableGeneratorJobListener.class);

    public static final String LIMIT = " LIMIT 1";

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openMRSJdbcTemplate;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        try {
            createTable(jobExecution.getJobInstance().getJobName());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            jobExecution.stop();
        }
    }

    private void createTable(String jobName) {
        tableGeneratorStep.createTables(Arrays.asList(getTableDataForMart(jobName)));
    }

    public TableData getTableDataForMart(String jobName) {
        JobDefinition jobDefinition = getJobDefinitionByName(jobName);
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        TableData tableData = openMRSJdbcTemplate.query(jobDefinition.getReaderSql() + LIMIT, resultSetExtractor);
        tableData.setName(jobDefinition.getTableName());
        tableData.getColumns().forEach(tableColumn -> tableColumn
                .setType(Constants.getPostgresDataTypeFor(tableColumn.getType())));
        return tableData;
    }

    private JobDefinition getJobDefinitionByName(String jobName) {
        Optional<JobDefinition> optionalJobDefinition = jobDefinitionReader.getJobDefinitions().stream()
                .filter(tempJobDefinition -> tempJobDefinition.getName().equals(jobName)).findFirst();
        return optionalJobDefinition.orElseGet(JobDefinition::new);
    }
}
