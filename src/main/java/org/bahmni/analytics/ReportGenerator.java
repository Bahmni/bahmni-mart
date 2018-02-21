package org.bahmni.analytics;

import org.bahmni.analytics.helper.FreeMarkerEvaluator;
import org.bahmni.analytics.report.JobResult;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReportGenerator {

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private FreeMarkerEvaluator<List<JobResult>> evaluator;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    public String generateReport() {
        List<JobInstance> jobInstanceList = jobExplorer
                .findJobInstancesByJobName(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME, 0, 20);
        return evaluator.evaluate("report.ftl",
                transformJobExecutionsToReport(getJobExecutionsForInstances(jobInstanceList)));
    }

    private List<JobExecution> getJobExecutionsForInstances(List<JobInstance> jobInstanceList) {
        List<JobExecution> executions = new ArrayList<>();
        for (JobInstance jobInstance : jobInstanceList) {
            executions.addAll(jobExplorer.getJobExecutions(jobInstance));
        }
        return executions;
    }

    private List<JobResult> transformJobExecutionsToReport(List<JobExecution> jobExecutions) {
        List<JobResult> jobResults = new ArrayList<>();
        for (JobExecution execution : jobExecutions) {
            String zipFileName = execution.getExecutionContext()
                    .getString(JobCompletionNotificationListener.OUTPUT_FILE_NAME_CONTEXT_KEY);
            jobResults.add(new JobResult(dateFormat.format(execution.getCreateTime()),
                    execution.getExitStatus().getExitCode(), zipFileName));
        }

        return jobResults;
    }

    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public void setEvaluator(FreeMarkerEvaluator<List<JobResult>> evaluator) {
        this.evaluator = evaluator;
    }
}
