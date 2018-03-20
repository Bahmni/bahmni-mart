package org.bahmni.mart.config;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exports.MetaDataExportStep;
import org.bahmni.mart.table.TableDataExtractor;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;

@Component
public class MetaDataStepConfigurer implements StepConfigurer {

    private static final String LIMIT = "LIMIT 1";

    @Autowired
    private ObjectFactory<MetaDataExportStep> metaDataExportStepObjectFactory;

    @Value("classpath:sql/metaDataCodeDictionary.sql")
    private Resource metaDataSqlResource;

    @Qualifier("openmrsJdbcTemplate")
    @Autowired
    private JdbcTemplate openmrsJDBCTemplate;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    private TableData tableData;

    @Override
    public void createTables() {
        tableGeneratorStep.createTables(Arrays.asList(tableData));
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport) {
        createTableData();
        MetaDataExportStep metaDataExportStep = metaDataExportStepObjectFactory.getObject();
        metaDataExportStep.setTableData(tableData);
        completeDataExport.next(metaDataExportStep.getStep());
    }

    private void createTableData() {
        String sql = convertResourceOutputToString(metaDataSqlResource);
        ResultSetExtractor<TableData> resultSetExtractor = new TableDataExtractor();
        sql = BatchUtils.constructSqlWithParameter(sql,"conceptReferenceSource",
                jobDefinitionReader.getConceptReferenceSource());
        tableData = openmrsJDBCTemplate.query(sql + LIMIT, resultSetExtractor);
        tableData.setName("meta_data_dictionary");
    }
}
