package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.FormStepConfigurer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Job;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ObsJobStrategyTest extends StepRegisterTestHelper {

    private ObsJobStrategy obsJobStrategy;

    @Mock
    private FormStepConfigurer formStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        obsJobStrategy = new ObsJobStrategy(formStepConfigurer);
        super.setUp(obsJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {

        Job actualJob = obsJobStrategy.getJob(jobDefinition);

        Assert.assertEquals(expectedJob, actualJob);

        verify(formStepConfigurer, times(1)).createTables();
        verify(formStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }

}