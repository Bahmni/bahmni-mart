package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class StepRegisterTestHelper {

    @Mock
    Job expectedJob;

    @Mock
    JobFlowBuilder jobFlowBuilder;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private JobBuilder jobBuilder;

    @Mock
    private Step step;

    @Mock
    private FlowJobBuilder flowJobBuilder;

    private String jobName = "jobName";

    private JobDefinition jobDefinition;

    public void setUp(JobStrategy jobStrategy, JobDefinition jobDefinition) throws Exception {

        this.jobDefinition = jobDefinition;

        setValuesForSuperClassMemberFields(jobStrategy, "jobBuilderFactory", jobBuilderFactory);
        setValuesForSuperClassMemberFields(jobStrategy, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);

        when(jobDefinition.getName()).thenReturn(jobName);
        when(jobBuilderFactory.get(jobName)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(step);
        when(jobBuilder.flow(step)).thenReturn(jobFlowBuilder);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        when(flowJobBuilder.build()).thenReturn(expectedJob);

    }

    public void verifyMockedCalls() {
        verify(jobDefinition, times(1)).getName();
        verify(jobBuilderFactory, times(1)).get(jobName);
        verify(jobBuilder, times(1)).incrementer(any(RunIdIncrementer.class));
        verify(jobBuilder, times(1)).preventRestart();
        verify(treatmentRegistrationBaseExportStep, times(1)).getStep();
        verify(jobBuilder, times(1)).flow(step);
        verify(jobFlowBuilder, times(1)).end();
        verify(flowJobBuilder, times(1)).build();
    }

}