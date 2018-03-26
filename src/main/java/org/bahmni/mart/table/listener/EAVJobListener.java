package org.bahmni.mart.table.listener;

import org.bahmni.mart.config.job.EavAttributes;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EAVJobListener extends AbstractJobListener {
    private static final Logger log = LoggerFactory.getLogger(EAVJobListener.class);

    @Override
    public TableData getTableDataForMart(String jobName) {
        TableData tableData = new TableData();
        JobDefinition jobDefinition = jobDefinitionReader.getJobDefinitionByName(jobName);
        tableData.setName(jobDefinition.getTableName());
        tableData.addAllColumns(getColumns(jobDefinition));

        return tableData;
    }

    @Override
    protected void logError(Exception e) {
        log.error(e.getMessage(), e);
    }

    private List<TableColumn> getColumns(JobDefinition jobConfiguration) {
        EavAttributes eavAttributes = jobConfiguration.getEavAttributes();
        List<String> pivotColumns = getPivotColumns(eavAttributes.getAttributeTypeTableName());

        ArrayList<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn(eavAttributes.getPrimaryKey(), "integer", true, null));
        pivotColumns.forEach(columnTitle -> tableColumns.add(new TableColumn(columnTitle, "text", false, null)));

        return tableColumns;
    }

    private List<String> getPivotColumns(String tableName) {
        String sql = String.format("select name from %s;", tableName);
        return openMRSJdbcTemplate.queryForList(sql, String.class);
    }
}
