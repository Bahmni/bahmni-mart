package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableGeneratorJobListener;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SimpleJobTemplate.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("openmrsDb")
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
        return stepBuilderFactory.get(String.format("%s_Step", jobConfiguration.getName()))
                .<Map<String, Object>, Map<String, Object>>chunk(jobConfiguration.getChunkSizeToRead())
                .reader(openMRSDataReader(jobConfiguration))
                .processor(getProcessor(jobConfiguration))
                .writer(getWriter())
                .build();
    }

    private TableDataProcessor getProcessor(JobDefinition jobConfiguration) {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        try {
            tableDataForMart = tableGeneratorJobListener.getTableDataForMart(jobConfiguration.getName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        tableDataProcessor.setTableData(tableDataForMart);
        return tableDataProcessor;
    }

    private TableRecordWriter getWriter() {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(tableDataForMart);
        return writer;
    }

    private JdbcCursorItemReader<Map<String, Object>> openMRSDataReader(JobDefinition jobConfiguration) {
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(openMRSDataSource);
        String readerSQLAfterIgnoringColumns = JobDefinitionUtil.getReaderSQLByIgnoringColumns(jobConfiguration);
        reader.setSql(readerSQLAfterIgnoringColumns);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }
}
