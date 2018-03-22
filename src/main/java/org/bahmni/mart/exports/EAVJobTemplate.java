package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
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
public class EAVJobTemplate {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("openmrsDb")
    private DataSource openMRSDataSource;

    @Autowired
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    private EAVJobListener eavJobListener;

    private TableData tableDataForMart;

    public Job buildJob(JobDefinition jobConfiguration) {
        return jobBuilderFactory.get(jobConfiguration.getName())
                .incrementer(new RunIdIncrementer())
                .listener(eavJobListener)
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
        tableDataForMart = eavJobListener.getTableData(jobConfiguration.getName());
        tableDataProcessor.setTableData(tableDataForMart);
        return tableDataProcessor;
    }

    private TableRecordWriter getWriter() {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(tableDataForMart);
        return writer;
    }

    private JdbcCursorItemReader<Map<String, Object>> openMRSDataReader(JobDefinition jobConfiguration) {
        TableData tableData = eavJobListener.getTableData(jobConfiguration.getName());
        String readerSql = freeMarkerEvaluator.evaluate("attribute.ftl",
                new EAV(tableData, jobConfiguration.getEavAttributes()));
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(openMRSDataSource);
        reader.setSql(readerSql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }


}
