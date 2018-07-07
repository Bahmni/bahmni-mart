package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.DiagnosesStepConfigurer;
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
public class DiagnosesJobStrategyTest extends StepRegisterTestHelper {

    private DiagnosesJobStrategy diagnosesJobStrategy;

    @Mock
    private DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        diagnosesJobStrategy = new DiagnosesJobStrategy(diagnosesStepConfigurer);
        super.setUp(diagnosesJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {
        assertEquals(expectedJob, diagnosesJobStrategy.getJob(jobDefinition));

        verify(diagnosesStepConfigurer, times(1)).createTables();
        verify(diagnosesStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }

}