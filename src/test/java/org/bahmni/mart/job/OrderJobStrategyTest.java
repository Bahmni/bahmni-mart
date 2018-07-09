package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
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
public class OrderJobStrategyTest extends StepRegisterTestHelper {

    private OrderJobStrategy orderJobStrategy;

    @Mock
    private OrderStepConfigurer orderStepConfigurer;

    @Mock
    private JobDefinition jobDefinition;

    @Before
    public void setUp() throws Exception {
        orderJobStrategy = new OrderJobStrategy(orderStepConfigurer);
        super.setUp(orderJobStrategy, jobDefinition);
    }

    @After
    public void verifyMockedCalls() {
        super.verifyMockedCalls();
    }

    @Test
    public void shouldReturnAJob() {
        assertEquals(expectedJob, orderJobStrategy.getJob(jobDefinition));

        verify(orderStepConfigurer, times(1)).createTables();
        verify(orderStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
    }

}