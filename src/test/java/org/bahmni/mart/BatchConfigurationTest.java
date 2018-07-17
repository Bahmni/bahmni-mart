package org.bahmni.mart;

import org.bahmni.mart.executors.MartExecutor;
import org.bahmni.mart.executors.MartJobExecutor;
import org.bahmni.mart.executors.MartViewExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    private BatchConfiguration batchConfiguration;

    @Mock
    private MartJobExecutor martJobExecutor;

    @Mock
    private MartViewExecutor martViewExecutor;

    @Before
    public void setUp() throws Exception {
        batchConfiguration = new BatchConfiguration();
        List<MartExecutor> martExecutors = Arrays.asList(martJobExecutor, martViewExecutor);
        setValuesForMemberFields(batchConfiguration, "martExecutors", martExecutors);
    }

    @Test
    public void shouldExecuteAllMartExecutors() throws Exception {
        setValuesForMemberFields(batchConfiguration, "shouldRunBatchJob", true);

        batchConfiguration.run();

        verify(martJobExecutor, times(1)).execute();
        verify(martViewExecutor, times(1)).execute();

    }

    @Test
    public void shouldNotRunBatchJobIfShouldRunBatchJobIsSetToFalse() throws Exception {
        setValuesForMemberFields(batchConfiguration, "shouldRunBatchJob", false);

        batchConfiguration.run();

        verify(martJobExecutor, never()).execute();
        verify(martViewExecutor, never()).execute();
    }
}
