package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.RspStepConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RSPJobStrategyTest extends StepRegisterTestHelper {
    private RSPJobStrategy rspJobStrategy;

    @Mock
    private RspStepConfigurer rspStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        rspJobStrategy = new RSPJobStrategy(rspStepConfigurer);
        super.setUp(rspJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {

        Job actualJob = rspJobStrategy.getJob(jobDefinition);

        assertEquals(expectedJob, actualJob);

        verify(rspStepConfigurer, times(1)).createTables();
        verify(rspStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }
}