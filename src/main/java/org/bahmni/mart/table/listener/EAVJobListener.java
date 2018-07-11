package org.bahmni.mart.table.listener;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.config.job.model.EavAttributes;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = "prototype")
public class EAVJobListener extends AbstractJobListener {
    private static final Logger log = LoggerFactory.getLogger(EAVJobListener.class);

    private CodesProcessor codesProcessor;
    private TableData tableData;

    @Override
    protected void setUpPreProcessorData() {
        if (codesProcessor != null)
            codesProcessor.setUpCodesData();
    }

    @Override
    public TableData getTableDataForMart(String jobName) {
        return getTableDataForMart(jobDefinitionReader.getJobDefinitionByName(jobName));
    }

    @Override
    public TableData getTableDataForMart(JobDefinition jobDefinition) {
        if (tableData != null) {
            return tableData;
        }
        tableData = new TableData();
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
        List<String> columnsToIgnore = jobConfiguration.getColumnsToIgnore();
        List<String> pivotColumns = getPivotColumns(eavAttributes.getAttributeTypeTableName());

        ArrayList<TableColumn> tableColumns = CollectionUtils.isEmpty(columnsToIgnore) ?
                getAllColumns(pivotColumns) : getColumnsExceptIgnored(columnsToIgnore, pivotColumns);

        tableColumns.add(0, new TableColumn(eavAttributes.getPrimaryKey(), "integer", true, null));

        return tableColumns;
    }

    private ArrayList<TableColumn> getColumnsExceptIgnored(List<String> columnsToIgnore, List<String> pivotColumns) {
        ArrayList<TableColumn> tableColumns = new ArrayList<>();
        pivotColumns.forEach(columnTitle -> {
            if (!columnsToIgnore.contains(columnTitle)) {
                tableColumns.add(new TableColumn(columnTitle, "text", false, null));
            }
        });

        return tableColumns;
    }

    private ArrayList<TableColumn> getAllColumns(List<String> pivotColumns) {
        ArrayList<TableColumn> tableColumns = new ArrayList<>();
        pivotColumns.forEach(columnTitle ->
                tableColumns.add(new TableColumn(columnTitle, "text", false, null))
        );
        return tableColumns;
    }

    private List<String> getPivotColumns(String tableName) {
        String sql = String.format("select name from %s;", tableName);
        return openMRSJdbcTemplate.queryForList(sql, String.class);
    }

    public void setCodesProcessor(CodesProcessor codesProcessor) {
        this.codesProcessor = codesProcessor;
    }
}
