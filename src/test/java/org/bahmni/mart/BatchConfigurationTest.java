package org.bahmni.mart;

import org.bahmni.mart.executors.MartExecutor;
import org.bahmni.mart.executors.MartJobExecutor;
import org.bahmni.mart.executors.MartViewExecutor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchConfigurationTest {

    @Test
    public void shouldExecuteAllMartExecutors() throws Exception {

        BatchConfiguration batchConfiguration = new BatchConfiguration();

        MartExecutor martJobExecutor = mock(MartJobExecutor.class);
        MartExecutor martViewExecutor = mock(MartViewExecutor.class);

        List<MartExecutor> martExecutors = Arrays.asList(martJobExecutor, martViewExecutor);
        setValuesForMemberFields(batchConfiguration, "martExecutors", martExecutors);

        batchConfiguration.run();

        verify(martJobExecutor, times(1)).execute();
        verify(martViewExecutor, times(1)).execute();

    }
}
