package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.BacteriologyStepConfigurer;
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
public class BacteriologyJobStrategyTest extends StepRegisterTestHelper {

    private BacteriologyJobStrategy bacteriologyJobStrategy;

    @Mock
    private BacteriologyStepConfigurer bacteriologyStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        bacteriologyJobStrategy = new BacteriologyJobStrategy(bacteriologyStepConfigurer);
        super.setUp(bacteriologyJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {
        assertEquals(expectedJob, bacteriologyJobStrategy.getJob(jobDefinition));

        verify(bacteriologyStepConfigurer, times(1)).createTables();
        verify(bacteriologyStepConfigurer, times(1)).registerSteps(jobFlowBuilder,
                jobDefinition);

    }
}