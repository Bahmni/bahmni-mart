package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.listener.JobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.ObjectFactory;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class JobTemplateTest {
    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private JobListener jobListener;

    @Mock
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    private JobTemplate jobTemplate;

    @Before
    public void setUp() throws Exception {
        mockStatic(JobDefinitionUtil.class);
        jobTemplate = new JobTemplate();
        setValuesForMemberFields(jobTemplate, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(jobTemplate, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(jobTemplate, "recordWriterObjectFactory", recordWriterObjectFactory);
    }

    @Test
    public void shouldBuildAndReturnAJOb() throws NoSuchFieldException, IllegalAccessException {
        JobDefinition jobDefinition = new JobDefinition();
        String testJobName = "testJob";
        jobDefinition.setName(testJobName);
        jobDefinition.setChunkSizeToRead(100);
        String readerSql = "select * from table";
        jobDefinition.setReaderSql(readerSql);

        Job expectedJob = setUpStepBuilder(setUpJobBuilder(testJobName));
        Job actualJob = jobTemplate.buildJob(jobDefinition, jobListener, readerSql);
        when(JobDefinitionUtil.getReaderSQLByIgnoringColumns(any(), anyString())).thenReturn(readerSql);

        assertEquals(expectedJob, actualJob);
        verify(jobBuilderFactory, times(1)).get(testJobName);
        verify(stepBuilderFactory, times(1)).get(String.format("%s Step", testJobName));
        verify(jobListener, times(1)).getTableDataForMart(testJobName);
        verify(recordWriterObjectFactory, times(1)).getObject();

        verifyStatic(times(1));
        JobDefinitionUtil.getReaderSQLByIgnoringColumns(null, readerSql);
    }

    private FlowJobBuilder setUpJobBuilder(String testJobName) {
        JobBuilder jobBuilder = mock(JobBuilder.class);
        when(jobBuilderFactory.get(testJobName)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.listener(jobListener)).thenReturn(jobBuilder);
        JobFlowBuilder jobFlowBuilder = mock(JobFlowBuilder.class);
        when(jobBuilder.flow(any())).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        return flowJobBuilder;
    }

    private Job setUpStepBuilder(FlowJobBuilder flowJobBuilder) {
        StepBuilder stepBuilder = mock(StepBuilder.class);
        when(stepBuilderFactory.get(any())).thenReturn(stepBuilder);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        TableRecordWriter tableWriter = mock(TableRecordWriter.class);
        when(recordWriterObjectFactory.getObject()).thenReturn(tableWriter);
        Job expectedJob = mock(Job.class);
        when(flowJobBuilder.build()).thenReturn(expectedJob);
        return expectedJob;
    }

}