package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
import org.bahmni.mart.exports.writer.RemovalWriter;
import org.bahmni.mart.exports.writer.TableRecordWriter;
import org.bahmni.mart.table.PreProcessor;
import org.bahmni.mart.table.TableDataProcessor;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.AbstractJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import javax.sql.DataSource;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

public class JobTemplate {

    private static Logger logger = LoggerFactory.getLogger(JobTemplate.class);

    private static final String VOIDED_MANIPULATION_REGEX = "(((\\s+AND\\s*)?|(\\s+WHERE\\s*)?)?\\s+(\\w+\\.)?" +
            "((voided)|(retired))\\s*=\\s*(FALSE|0)(?!\\w))";

    private static final Pattern VOIDED_MANIPULATION_PATTERN = compile(VOIDED_MANIPULATION_REGEX, CASE_INSENSITIVE);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Autowired
    private IncrementalStrategyContext incrementalStrategyContext;

    @Autowired
    private ObjectFactory<RemovalWriter> removalWriterObjectFactory;

    @Autowired
    @Qualifier("openmrsDb")
    private DataSource openMRSDataSource;

    private TableData tableDataForMart;

    private PreProcessor preProcessor;

    protected Job buildJob(JobDefinition jobConfiguration, AbstractJobListener listener, String readerSql) {
        setTableData(listener, jobConfiguration);

        JobBuilder jobBuilder = jobBuilderFactory.get(jobConfiguration.getName())
                .incrementer(new RunIdIncrementer())
                .listener(listener);

        return getJob(jobConfiguration, readerSql, jobBuilder);
    }

    private void setTableData(AbstractJobListener listener, JobDefinition jobConfiguration) {
        try {
            tableDataForMart = listener.getTableDataForMart(jobConfiguration.getName());
        } catch (BadSqlGrammarException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Job getJob(JobDefinition jobConfiguration, String readerSql, JobBuilder jobBuilder) {
        if (!isMetaDataChanged(jobConfiguration)) {
            return jobBuilder
                    .flow(deleteDataStep(jobConfiguration, updateReaderSqlToReadVoidedData(readerSql)))
                    .next(loadData(jobConfiguration, readerSql))
                    .end().build();
        }
        return jobBuilder.flow(loadData(jobConfiguration, readerSql)).end().build();
    }



    private String getStepName(String postfix, String jobName) {
        return String.format("%s %s", jobName, postfix);
    }

    private RemovalWriter getRemovalWriter(JobDefinition jobDefinition) {
        RemovalWriter writer = removalWriterObjectFactory.getObject();
        writer.setJobDefinition(jobDefinition);
        writer.setTableData(tableDataForMart);
        return writer;
    }

    private boolean isMetaDataChanged(JobDefinition jobConfiguration) {
        return isMetaDataChanged(jobConfiguration, incrementalStrategyContext.getStrategy(jobConfiguration.getType()));
    }

    private boolean isMetaDataChanged(JobDefinition jobDefinition, IncrementalUpdateStrategy strategyContext) {
        return strategyContext
                .isMetaDataChanged(jobDefinition.getTableName(), jobDefinition.getName());
    }

    private String updateReaderSqlToReadVoidedData(String readerSql) {
        return VOIDED_MANIPULATION_PATTERN.matcher(readerSql).replaceAll("");
    }

    private Step deleteDataStep(JobDefinition jobDefinition, String voidedSql) {
        return stepBuilderFactory.get(getStepName("Removal Step", jobDefinition.getName()))
                .<Map<String, Object>, Map<String, Object>>chunk(jobDefinition.getChunkSizeToRead())
                .reader(getReader(voidedSql))
                .writer(getRemovalWriter(jobDefinition))
                .build();
    }

    private Step loadData(JobDefinition jobConfiguration, String readerSql) {
        return stepBuilderFactory.get(getStepName("Step", jobConfiguration.getName()))
                .<Map<String, Object>, Map<String, Object>>chunk(jobConfiguration.getChunkSizeToRead())
                .reader(getReader(readerSql))
                .processor(getProcessor())
                .writer(getWriter(jobConfiguration))
                .build();
    }

    private TableDataProcessor getProcessor() {
        TableDataProcessor tableDataProcessor = new TableDataProcessor();
        if (preProcessor != null) {
            tableDataProcessor.setPreProcessor(preProcessor);
        }
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

        return isMetaDataChanged(jobDefinition, strategyContext) ? readerSql :
                strategyContext.updateReaderSql(readerSql, jobDefinition.getName(),
                        incrementalUpdateConfig.getUpdateOn());
    }
}
