package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableGeneratorJobListener;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
public class SimpleJobTemplate {

    private static final String STEP = "step-1";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("mysqlDb")
    private DataSource openMRSDataSource;

    @Autowired
    private TableGeneratorJobListener tableGeneratorJobListener;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    private TableData tableDataForMart;

    public Job buildJob(JobDefinition jobConfiguration) {
        return jobBuilderFactory.get(jobConfiguration.getName())
                .incrementer(new RunIdIncrementer())
                .listener(tableGeneratorJobListener)
                .flow(loadData(jobConfiguration))
                .end().build();
    }

    private Step loadData(JobDefinition jobConfiguration) {
        return stepBuilderFactory.get(STEP)
                .<Map<String, Object>, Map<String, Object>>chunk(jobConfiguration.getChunkSizeToRead())
                .reader(openMRSDataReader(jobConfiguration))
                .processor(getProcessor(jobConfiguration))
                .writer(martWriter(jobConfiguration))
                .build();
    }

    private TableDataProcessor getProcessor(JobDefinition jobConfiguration) {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        tableDataForMart = tableGeneratorJobListener.getTableDataForMart(jobConfiguration.getName());
        tableDataProcessor.setTableData(tableDataForMart);
        return tableDataProcessor;
    }

    private TableRecordWriter martWriter(JobDefinition jobConfiguration) {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(tableDataForMart);
        return writer;
    }

    private JdbcCursorItemReader<Map<String, Object>> openMRSDataReader(JobDefinition jobConfiguration) {
        String readerSql = jobConfiguration.getReaderSql();
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(openMRSDataSource);
        reader.setSql(readerSql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }


}
