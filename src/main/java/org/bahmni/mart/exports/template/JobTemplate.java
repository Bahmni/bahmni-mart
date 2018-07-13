package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
import org.bahmni.mart.table.PreProcessor;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.exports.writer.TableRecordWriter;
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

import static java.util.Objects.isNull;

public class JobTemplate {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    private IncrementalStrategyContext incrementalStrategyContext;

    @Autowired
    @Qualifier("openmrsDb")
    private DataSource openMRSDataSource;

    private TableData tableDataForMart;

    private PreProcessor preProcessor;

    protected Job buildJob(JobDefinition jobConfiguration, AbstractJobListener listener, String readerSql) {
        return jobBuilderFactory.get(jobConfiguration.getName())
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(loadData(jobConfiguration, listener, readerSql))
                .end().build();
    }

    private Step loadData(JobDefinition jobConfiguration, AbstractJobListener listener, String readerSql) {
        return stepBuilderFactory.get(String.format("%s Step", jobConfiguration.getName()))
                .<Map<String, Object>, Map<String, Object>>chunk(jobConfiguration.getChunkSizeToRead())
                .reader(getReader(readerSql))
                .processor(getProcessor(jobConfiguration, listener))
                .writer(getWriter(jobConfiguration))
                .build();
    }

    private TableDataProcessor getProcessor(JobDefinition jobConfiguration, AbstractJobListener listener) {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        if (preProcessor != null) {
            tableDataProcessor.setPreProcessor(preProcessor);
        }
        tableDataForMart = listener.getTableDataForMart(jobConfiguration.getName());
        tableDataProcessor.setTableData(tableDataForMart);
        return tableDataProcessor;
    }

    private TableRecordWriter getWriter(JobDefinition jobDefinition) {
        TableRecordWriter writer = recordWriterObjectFactory.getObject();
        writer.setTableData(tableDataForMart);
        writer.setJobDefinition(jobDefinition);
        return writer;
    }

    private JdbcCursorItemReader<Map<String, Object>> getReader(String readerSql) {
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(openMRSDataSource);
        reader.setSql(readerSql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    protected void setPreProcessor(PreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    protected String getUpdatedReaderSql(JobDefinition jobDefinition, String readerSql) {
        IncrementalUpdateConfig incrementalUpdateConfig = jobDefinition.getIncrementalUpdateConfig();

        if (isNull(incrementalUpdateConfig))
            return readerSql;

        IncrementalUpdateStrategy strategyContext = incrementalStrategyContext.getStrategy(jobDefinition.getType());
        boolean metaDataChanged = strategyContext
                .isMetaDataChanged(jobDefinition.getTableName(), jobDefinition.getName());

        return metaDataChanged ? readerSql : strategyContext.updateReaderSql(readerSql, jobDefinition.getName(),
                incrementalUpdateConfig.getUpdateOn());
    }
}
