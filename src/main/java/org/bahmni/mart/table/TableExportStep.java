package org.bahmni.mart.table;

import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

import static org.bahmni.mart.BatchUtils.stepNumber;

@Component
@Scope(value = "prototype")
@Primary
public class TableExportStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private FreeMarkerEvaluator<TableData> tableRecordHolderFreeMarkerEvaluator;

    private TableData tableData;

    public Step getStep() {
        return stepBuilderFactory.get(getStepName())
                .<Map<String, Object>, Map<String, Object>>chunk(100)
                .reader(getReader())
                .processor(getProcessor())
                .writer(getWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String, Object>> getReader() {
        String sql = getJDBCReaderSqlString();
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private TableDataProcessor getProcessor() {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        tableDataProcessor.setTableData(getTableData());
        return tableDataProcessor;
    }

    private TableRecordWriter getWriter() {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(getTableData());
        return writer;
    }

    public String getStepName() {
        stepNumber++;
        String tableName = getTableData() != null ? getTableData().getName() : null;
        String formName = "Step-" + stepNumber + " " + tableName;
        return formName.substring(0, Math.min(formName.length(), 100));
    }

    protected TableData getTableData() {
        return this.tableData;
    }

    private String getJDBCReaderSqlString() {
        return tableRecordHolderFreeMarkerEvaluator.evaluate("reader.ftl", getTableData());
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

}
