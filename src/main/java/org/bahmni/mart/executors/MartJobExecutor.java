package org.bahmni.mart.executors;

import org.bahmni.mart.config.group.GroupedJob;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.job.JobContext;
import org.bahmni.mart.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Order(value = MartExecutionOrder.JOB)
public class MartJobExecutor implements MartExecutor {

    private static final Logger log = LoggerFactory.getLogger(MartJobExecutor.class);

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobContext jobContext;

    @Autowired
    private GroupedJob groupedJob;

    private List<JobExecution> jobExecutions = new ArrayList<>();

    @Autowired(required = false)
    private MailService mailService;

    @Override
    public void execute() {

        List<JobDefinition> allJobDefinitions = jobDefinitionReader.getJobDefinitions();

        validateJobDefinitions(allJobDefinitions);

        allJobDefinitions = groupedJob.getJobDefinitionsBySkippingGroupedTypeJobs(allJobDefinitions);

        List<Job> jobs = allJobDefinitions.stream().map(jobDefinition -> jobContext.getJob(jobDefinition))
                .filter(Objects::nonNull).collect(Collectors.toList());

        launchJobs(jobs);
        if (mailService != null) {
            mailService.sendJobsStatus(getFailedJobNames());
        }
    }

    private void validateJobDefinitions(List<JobDefinition> jobDefinitions) {
        if (!JobDefinitionValidator.validate(jobDefinitions))
            throw new InvalidJobConfiguration();
    }

    private void launchJobs(List<Job> jobs) {

        jobs.forEach(job -> {
            try {
                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                jobParametersBuilder.addDate(job.getName(), new Date());
                JobExecution jobExecution = jobLauncher.run(job, jobParametersBuilder.toJobParameters());
                jobExecutions.add(jobExecution);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        });
    }

    private List<String> getFailedJobNames() {
        return jobExecutions.stream().filter(jobExecution -> !jobExecution.getExitStatus().getExitCode()
                .equals(ExitStatus.COMPLETED.getExitCode()))
                .map(jobExecution -> jobExecution.getJobInstance().getJobName()).collect(Collectors.toList());
    }
}
