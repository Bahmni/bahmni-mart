package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.EAVJobData;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EAVJobListener extends JobExecutionListenerSupport {
    private static final Logger log = LoggerFactory.getLogger(TableGeneratorJobListener.class);

    @Autowired
    @Qualifier("openmrsJdbcTemplate")
    private JdbcTemplate openMRSJdbcTemplate;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

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
        tableGeneratorStep.createTables(Arrays.asList(getTableData(jobName)));
    }

    public TableData getTableData(String jobName) {
        TableData tableData = new TableData();
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        tableData.setName(jobDefinition.getTableName());
        tableData.addAllColumns(getColumns(jobDefinition));

        return tableData;
    }

    private List<TableColumn> getColumns(JobDefinition jobConfiguration) {
        EAVJobData eavJobData = jobConfiguration.getEavAttributes();
        List<String> pivotColumns = getPivotColumns(eavJobData.getAttributeTypeTableName());

        ArrayList<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn(eavJobData.getPrimaryKey(), "integer", true, null));
        pivotColumns.forEach(columnTitle -> tableColumns.add(new TableColumn(columnTitle, "text", false, null)));

        return tableColumns;
    }

    private List<String> getPivotColumns(String tableName) {
        String sql = String.format("select name from %s;", tableName);
        return openMRSJdbcTemplate.queryForList(sql, String.class);
    }
}
