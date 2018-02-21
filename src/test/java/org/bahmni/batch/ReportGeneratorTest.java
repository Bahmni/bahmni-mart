package org.bahmni.batch;

import org.bahmni.batch.helper.FreeMarkerEvaluator;
import org.bahmni.batch.report.JobResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReportGeneratorTest {

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private FreeMarkerEvaluator<List<JobResult>> evaluator;

    @Captor
    private ArgumentCaptor<List<JobResult>> jobResultCaptor;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void ensureThatEvaluatorReturnsAHtml() {
        JobInstance jobInstance1 = new JobInstance(1L, "firstJob");
        JobInstance jobInstance2 = new JobInstance(2L, "secondJob");

        JobExecution jobExecution1a = new JobExecution(11L);
        jobExecution1a.getExecutionContext()
                .put(JobCompletionNotificationListener.OUTPUT_FILE_NAME_CONTEXT_KEY, "abc.zip");

        JobExecution jobExecution1b = new JobExecution(12L);
        jobExecution1b.getExecutionContext()
                .put(JobCompletionNotificationListener.OUTPUT_FILE_NAME_CONTEXT_KEY, "def.zip");

        JobExecution jobExecution2a = new JobExecution(21L);
        jobExecution2a.getExecutionContext()
                .put(JobCompletionNotificationListener.OUTPUT_FILE_NAME_CONTEXT_KEY, "ghi.zip");

        when(jobExplorer.findJobInstancesByJobName(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME, 0, 20))
                .thenReturn(Arrays.asList(jobInstance1, jobInstance2));
        when(jobExplorer.getJobExecutions(jobInstance1)).thenReturn(Arrays.asList(jobExecution1a, jobExecution1b));
        when(jobExplorer.getJobExecutions(jobInstance2)).thenReturn(Arrays.asList(jobExecution2a));
        when(evaluator.evaluate(eq("report.ftl"), jobResultCaptor.capture())).thenReturn("<html>1</html>");

        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.setEvaluator(evaluator);
        reportGenerator.setJobExplorer(jobExplorer);
        String result = reportGenerator.generateReport();
        assertEquals("<html>1</html>", result);

        List<JobResult> actual = jobResultCaptor.getValue();
        assertEquals(3, actual.size());
        assertEquals("abc.zip", actual.get(0).getZipFileName());
        assertEquals("def.zip", actual.get(1).getZipFileName());
        assertEquals("ghi.zip", actual.get(2).getZipFileName());
    }


}
