package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.AbstractJobListener;
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

import javax.sql.DataSource;
import java.util.Map;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getReaderSQLByIgnoringColumns;

public class JobTemplate {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    @Qualifier("openmrsDb")
    private DataSource openMRSDataSource;

    private TableData tableDataForMart;

    Job buildJob(JobDefinition jobConfiguration, AbstractJobListener listener, String readerSql) {
        return jobBuilderFactory.get(jobConfiguration.getName())
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(loadData(jobConfiguration, listener,
                        getReaderSQLByIgnoringColumns(jobConfiguration.getColumnsToIgnore(), readerSql)))
                .end().build();
    }

    private Step loadData(JobDefinition jobConfiguration, AbstractJobListener listener, String readerSql) {
        return stepBuilderFactory.get(String.format("%s Step", jobConfiguration.getName()))
                .<Map<String, Object>, Map<String, Object>>chunk(jobConfiguration.getChunkSizeToRead())
                .reader(getReader(readerSql))
                .processor(getProcessor(jobConfiguration, listener))
                .writer(getWriter())
                .build();
    }

    private TableDataProcessor getProcessor(JobDefinition jobConfiguration, AbstractJobListener listener) {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        tableDataForMart = listener.getTableDataForMart(jobConfiguration.getName());
        tableDataProcessor.setTableData(tableDataForMart);
        return tableDataProcessor;
    }

    private TableRecordWriter getWriter() {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(tableDataForMart);
        return writer;
    }

    private JdbcCursorItemReader<Map<String, Object>> getReader(String readerSql) {
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(openMRSDataSource);
        reader.setSql(readerSql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }
}
