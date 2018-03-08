package org.bahmni.mart.config;

import org.bahmni.mart.exports.ProgramMetaDataGenerator;
import org.bahmni.mart.table.TableExportStep;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProgramDataStepConfigurer implements StepConfigurer {

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private ProgramMetaDataGenerator programMetaDataGenerator;

    @Autowired
    private ObjectFactory<TableExportStep> tablesExportStepObjectFactory;

    @Override
    public void createTables() {
        tableGeneratorStep.createTables(programMetaDataGenerator.getTableDataList());
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport) {

        List<TableData> tableDataList = programMetaDataGenerator.getTableDataList();
        for (TableData tableData : tableDataList) {
            TableExportStep tablesExportStep = tablesExportStepObjectFactory.getObject();
            tablesExportStep.setTableData(tableData);
            completeDataExport.next(tablesExportStep.getStep());
        }
    }
}
