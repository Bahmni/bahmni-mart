package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.model.IncrementalUpdateConfig;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.exports.updatestrategy.IncrementalUpdateStrategy;
import org.bahmni.mart.exports.writer.RemovalWriter;
import org.bahmni.mart.exports.writer.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.AbstractJobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest(JobTemplate.class)
@RunWith(PowerMockRunner.class)
public class JobTemplateTest {

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private AbstractJobListener jobListener;

    @Mock
    private TableRecordWriter tableRecordWriter;

    @Mock
    private RemovalWriter removalWriter;

    @Mock
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Mock
    private ObjectFactory<RemovalWriter> removalWriterObjectFactory;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Mock
    private IncrementalUpdateStrategy incrementalUpdateStrategy;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private TableData tableData;

    @Mock
    private StepBuilder stepBuilder;

    @Mock
    private SimpleStepBuilder simpleStepBuilder;

    @Mock
    private JdbcCursorItemReader jdbcCursorItemReader;

    @Mock
    private SimpleStepBuilder simpleStepBuilderWithProcessor;

    @Mock
    private JobFlowBuilder jobFlowBuilder;

    @Mock
    private FlowJobBuilder flowJobBuilder;

    @Mock
    private JobBuilder jobBuilder;

    @Mock
    private Job job;

    @Mock
    private IncrementalUpdateConfig incrementalUpdateConfig;

    private JobTemplate jobTemplate;

    private static final String TEST_JOB_NAME = "testJob";
    private static final String READER_SQL = "select * from table where abc.Voided=FAlse";
    private static final String JOB_TYPE = "jobType";
    private static final String TABLE_NAME = "tableName";
    private static final String UPDATE_ON = "updateOn";

    @Before
    public void setUp() throws Exception {
        jobTemplate = new JobTemplate();
        setValuesForMemberFields(jobTemplate, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(jobTemplate, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(jobTemplate, "recordWriterObjectFactory", recordWriterObjectFactory);
        setValuesForMemberFields(jobTemplate, "incrementalStrategyContext", incrementalStrategyContext);
        setValuesForMemberFields(jobTemplate, "removalWriterObjectFactory", removalWriterObjectFactory);

        whenNew(JdbcCursorItemReader.class).withNoArguments().thenReturn(jdbcCursorItemReader);
        when(jobBuilderFactory.get(anyString())).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.listener(jobListener)).thenReturn(jobBuilder);
        when(jobBuilder.flow(any())).thenReturn(jobFlowBuilder);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);

        when(stepBuilderFactory.get(any())).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilderWithProcessor);
        when(simpleStepBuilderWithProcessor.writer(any())).thenReturn(simpleStepBuilderWithProcessor);

        when(flowJobBuilder.build()).thenReturn(job);

        when(recordWriterObjectFactory.getObject()).thenReturn(tableRecordWriter);

        when(jobDefinition.getName()).thenReturn(TEST_JOB_NAME);
        when(jobDefinition.getChunkSizeToRead()).thenReturn(100);
        when(jobDefinition.getType()).thenReturn(JOB_TYPE);
        when(jobDefinition.getTableName()).thenReturn(TABLE_NAME);
        when(jobDefinition.getIncrementalUpdateConfig()).thenReturn(incrementalUpdateConfig);

        when(incrementalUpdateConfig.getUpdateOn()).thenReturn(UPDATE_ON);
        when(incrementalUpdateConfig.getOpenmrsTableName()).thenReturn("openmrsTableName");
        when(incrementalUpdateConfig.getEventCategory()).thenReturn("event_category");

        when(incrementalStrategyContext.getStrategy(anyString())).thenReturn(incrementalUpdateStrategy);
        when(incrementalUpdateStrategy.isMetaDataChanged(anyString(), anyString())).thenReturn(true);

        when(jobListener.getTableDataForMart(TEST_JOB_NAME)).thenReturn(tableData);
    }

    @Test
    public void shouldBuildAndReturnAJob() throws Exception {
        Job actualJob = jobTemplate.buildJob(jobDefinition, jobListener, READER_SQL);

        assertEquals(job, actualJob);
        verify(jobBuilderFactory, times(1)).get(TEST_JOB_NAME);
        verify(stepBuilderFactory, times(1)).get(String.format("%s Insertion Step", TEST_JOB_NAME));
        verify(jobListener, times(1)).getTableDataForMart(TEST_JOB_NAME);
        verify(recordWriterObjectFactory, times(1)).getObject();
        verify(tableRecordWriter).setJobDefinition(jobDefinition);
        verify(tableRecordWriter).setTableData(tableData);

        verify(jobDefinition, atLeastOnce()).getName();
        verify(jobDefinition).getType();
        verify(jobDefinition, never()).getReaderSql();
        verify(jobDefinition, never()).getIncrementalUpdateConfig();
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(incrementalUpdateStrategy).isMetaDataChanged(TABLE_NAME, TEST_JOB_NAME);

        verify(simpleStepBuilder).reader(any());
        verify(simpleStepBuilder).processor(any());
        verify(simpleStepBuilder, never()).writer(any());
        verify(simpleStepBuilderWithProcessor).writer(any());
        verifyNew(JdbcCursorItemReader.class).withNoArguments();
        verify(jdbcCursorItemReader).setSql(READER_SQL);
    }


    @Test
    public void shouldAddRemovalStepIfMetadataIsSame() throws Exception {
        when(incrementalUpdateStrategy.isMetaDataChanged(anyString(), anyString())).thenReturn(false);
        when(removalWriterObjectFactory.getObject()).thenReturn(removalWriter);
        when(jobFlowBuilder.next(any(Step.class))).thenReturn(jobFlowBuilder);

        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);

        Job actualJob = jobTemplate.buildJob(jobDefinition, jobListener, READER_SQL);

        assertEquals(job, actualJob);
        verify(jobBuilderFactory).get(TEST_JOB_NAME);
        verify(jobBuilder).flow(any(Step.class));
        verify(jobFlowBuilder).next(any(Step.class));

        verify(stepBuilderFactory).get(String.format("%s Insertion Step", TEST_JOB_NAME));
        verify(stepBuilderFactory).get(String.format("%s Removal Step", TEST_JOB_NAME));
        verify(jobListener, times(1)).getTableDataForMart(TEST_JOB_NAME);
        verify(removalWriterObjectFactory, times(1)).getObject();
        verify(recordWriterObjectFactory, times(1)).getObject();

        verify(removalWriter).setJobDefinition(jobDefinition);
        verify(removalWriter).setTableData(tableData);

        verify(tableRecordWriter).setJobDefinition(jobDefinition);
        verify(tableRecordWriter).setTableData(tableData);

        verify(jobDefinition, atLeastOnce()).getName();
        verify(jobDefinition).getType();
        verify(jobDefinition, never()).getReaderSql();
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(incrementalUpdateStrategy).isMetaDataChanged(TABLE_NAME, TEST_JOB_NAME);


        verify(simpleStepBuilder, times(2)).reader(any());
        verify(simpleStepBuilder).processor(any());
        verify(simpleStepBuilder).writer(any());
        verify(simpleStepBuilderWithProcessor).writer(any());
        verifyNew(JdbcCursorItemReader.class, times(2)).withNoArguments();
        verify(jdbcCursorItemReader).setSql(READER_SQL);
        verify(jdbcCursorItemReader).setSql("select * from table");

    }

}