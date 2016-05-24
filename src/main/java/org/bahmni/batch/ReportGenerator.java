package org.bahmni.batch;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bahmni.batch.helper.FreeMarkerEvaluator;
import org.bahmni.batch.report.JobResult;
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

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

	public String generateReport(){
		List<JobInstance> jobInstanceList = jobExplorer.findJobInstancesByJobName(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME,1,20);
		List<JobResult> results = transformJobExecutionsToReport(getJobExecutionsForInstances(jobInstanceList));

		return evaluator.evaluate("report.ftl",results);
	}

	private List<JobExecution> getJobExecutionsForInstances(List<JobInstance> jobInstanceList){
		List<JobExecution> executions = new ArrayList<>();
		for(JobInstance jobInstance: jobInstanceList){
			executions.addAll(jobExplorer.getJobExecutions(jobInstance));
		}
		return executions;
	}

	private List<JobResult> transformJobExecutionsToReport(List<JobExecution> jobExecutions) {
		List<JobResult> jobResults = new ArrayList<>();
		for(JobExecution execution: jobExecutions){
			String zipFileName = execution.getExecutionContext().getString(JobCompletionNotificationListener.OUTPUT_FILE_NAME_CONTEXT_KEY);
			jobResults.add(new JobResult(dateFormat.format(execution.getCreateTime()),
					execution.getExitStatus().getExitCode(), zipFileName, getErrorMessageDetail(execution)));
		}

		return jobResults;
	}

	private String getErrorMessageDetail(JobExecution execution){
		StringBuilder exceptions = new StringBuilder();
		for(Throwable throwable: execution.getFailureExceptions()){
			exceptions.append(ExceptionUtils.getMessage(throwable));
		}
		return exceptions.toString();
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

	public void setEvaluator(FreeMarkerEvaluator<List<JobResult>> evaluator) {
		this.evaluator = evaluator;
	}
}
