package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;

@Component
public class MetaDataExportStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    private JobDefinition jobDefinition;

    @Value("classpath:sql/metaDataCodeDictionary.sql")
    private Resource metaDataSqlResource;

    private TableData tableData;

    public Step getStep() {
        return stepBuilderFactory.get("Meta Data Export Step")
                .<Map<String, Object>, Map<String, Object>>chunk(100)
                .reader(metaDataReader())
                .processor(metaDataProcessor())
                .writer(metaDataWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String, Object>> metaDataReader() {
        String sql = convertResourceOutputToString(metaDataSqlResource);
        sql = constructSqlWithParameter(sql,"conceptReferenceSource", jobDefinition.getConceptReferenceSource());
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private TableDataProcessor metaDataProcessor() {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        tableDataProcessor.setTableData(getTableData());
        return tableDataProcessor;
    }

    private TableRecordWriter metaDataWriter() {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(getTableData());
        return writer;
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }
}

