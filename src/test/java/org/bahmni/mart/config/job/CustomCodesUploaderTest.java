package org.bahmni.mart.config.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomCodesUploaderTest {

    @Mock
    private ObjectFactory objectFactory;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private CustomCodesTasklet customCodesTasklet;

    @Mock
    private Job job;

    private CustomCodesUploader customCodesUploader;

    @Before
    public void setUp() throws Exception {
        customCodesUploader = new CustomCodesUploader();
        setValuesForMemberFields(customCodesUploader, "objectFactory", objectFactory);
        setValuesForSuperClassMemberFields(customCodesUploader, "jobBuilderFactory", jobBuilderFactory);
        setValuesForSuperClassMemberFields(customCodesUploader, "stepBuilderFactory", stepBuilderFactory);
    }

    @Test
    public void shouldBuildJobWithGivenCustomCodesTasklet() {
        String jobName = "customCodes";
        when(jobDefinition.getName()).thenReturn(jobName);
        setUpJobWithTasklet(jobName);

        Job actualJob = customCodesUploader.buildJob(jobDefinition);

        assertEquals(job, actualJob);
        verify(customCodesTasklet, times(1)).setReaderFilePath(jobDefinition.getReaderFilePath());
    }

    private void setUpJobWithTasklet(String jobName) {
        when(objectFactory.getObject()).thenReturn(customCodesTasklet);

        StepBuilder stepBuilder = mock(StepBuilder.class);
        TaskletStepBuilder taskletStepBuilder = mock(TaskletStepBuilder.class);
        TaskletStep taskletStep = mock(TaskletStep.class);
        when(stepBuilderFactory.get(jobName)).thenReturn(stepBuilder);
        when(stepBuilder.tasklet(customCodesTasklet)).thenReturn(taskletStepBuilder);
        when(taskletStepBuilder.build()).thenReturn(taskletStep);

        JobBuilder jobBuilder = mock(JobBuilder.class);
        when(jobBuilderFactory.get(jobName)).thenReturn(jobBuilder);
        JobFlowBuilder jobFlowBuilder = mock(JobFlowBuilder.class);
        when(jobBuilder.flow(taskletStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        when(flowJobBuilder.build()).thenReturn(job);
    }
}