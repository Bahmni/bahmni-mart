package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.DispositionStepConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DispositionJobStrategyTest extends StepRegisterTestHelper {

    private DispositionJobStrategy dispositionJobStrategy;

    @Mock
    private DispositionStepConfigurer dispositionStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        dispositionJobStrategy = new DispositionJobStrategy(dispositionStepConfigurer);
        super.setUp(dispositionJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {
        assertEquals(expectedJob, dispositionJobStrategy.getJob(jobDefinition));

        verify(dispositionStepConfigurer, times(1)).createTables();
        verify(dispositionStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }
}