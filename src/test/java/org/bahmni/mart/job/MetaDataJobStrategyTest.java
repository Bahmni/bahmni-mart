package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.MetaDataStepConfigurer;
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
public class MetaDataJobStrategyTest extends StepRegisterTestHelper {

    private MetaDataJobStrategy metaDataJobStrategy;

    @Mock
    private MetaDataStepConfigurer metaDataStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        metaDataJobStrategy = new MetaDataJobStrategy(metaDataStepConfigurer);
        super.setUp(metaDataJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {
        assertEquals(expectedJob, metaDataJobStrategy.getJob(jobDefinition));

        verify(metaDataStepConfigurer, times(1)).createTables(jobDefinition);
        verify(metaDataStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }

}