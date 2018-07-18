package org.bahmni.mart;

import org.bahmni.mart.executors.MartExecutor;
import org.bahmni.mart.executors.MartJobExecutor;
import org.bahmni.mart.executors.MartProcedureExecutor;
import org.bahmni.mart.executors.MartViewExecutor;
import org.bahmni.mart.notification.MailSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    private BatchConfiguration batchConfiguration;

    @Mock
    private MartJobExecutor martJobExecutor;

    @Mock
    private MartProcedureExecutor martProcedureExecutor;

    @Mock
    private MartViewExecutor martViewExecutor;

    @Mock
    private MailSender mailSender;

    @Before
    public void setUp() throws Exception {
        batchConfiguration = new BatchConfiguration();
        List<MartExecutor> martExecutors = Arrays.asList(martJobExecutor, martProcedureExecutor, martViewExecutor);
        setValuesForMemberFields(batchConfiguration, "martExecutors", martExecutors);
        setValuesForMemberFields(batchConfiguration, "mailSender", mailSender);
    }

    @Test
    public void shouldExecuteAllMartExecutors() throws Exception {
        setValuesForMemberFields(batchConfiguration, "shouldRunBatchJob", true);

        batchConfiguration.run();

        verify(martJobExecutor, times(1)).execute();
        verify(martProcedureExecutor, times(1)).execute();
        verify(martViewExecutor, times(1)).execute();

    }

    @Test
    public void shouldNotRunBatchJobIfShouldRunBatchJobIsSetToFalse() throws Exception {
        setValuesForMemberFields(batchConfiguration, "shouldRunBatchJob", false);

        batchConfiguration.run();

        verify(martJobExecutor, never()).execute();
        verify(martViewExecutor, never()).execute();
    }

    @Test
    public void shouldCallMailServiceWithFailedJobs() throws Exception {
        setValuesForMemberFields(batchConfiguration, "shouldRunBatchJob", true);
        List<String> expectedFailedJobs = Arrays.asList("Job One", "Job Two", "Procedure One", "View One");
        when(martJobExecutor.getFailedJobs()).thenReturn(Arrays.asList("Job One", "Job Two"));
        when(martProcedureExecutor.getFailedJobs()).thenReturn(singletonList("Procedure One"));
        when(martViewExecutor.getFailedJobs()).thenReturn(singletonList("View One"));

        batchConfiguration.run();

        verify(martJobExecutor).getFailedJobs();
        verify(martProcedureExecutor).getFailedJobs();
        verify(martViewExecutor).getFailedJobs();
        verify(mailSender).sendMail(expectedFailedJobs);
    }
}
