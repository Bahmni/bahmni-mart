package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.RegStepConfigurer;
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
public class REGJobStrategyTest extends StepRegisterTestHelper {
    private REGJobStrategy regJobStrategy;

    @Mock
    private RegStepConfigurer regStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        regJobStrategy = new REGJobStrategy(regStepConfigurer);
        super.setUp(regJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {

        Job actualJob = regJobStrategy.getJob(jobDefinition);

        assertEquals(expectedJob, actualJob);

        verify(regStepConfigurer, times(1)).createTables();
        verify(regStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }
}