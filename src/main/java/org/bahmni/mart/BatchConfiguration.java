package org.bahmni.mart;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.job.CustomCodesUploader;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.procedure.ProcedureExecutor;
import org.bahmni.mart.config.stepconfigurer.BacteriologyStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.DiagnosesStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.DispositionStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.FormStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.MetaDataStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.RspStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.StepConfigurerContract;
import org.bahmni.mart.config.view.RspViewDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.bahmni.mart.config.view.ViewExecutor;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.mart.exports.template.EAVJobTemplate;
import org.bahmni.mart.exports.template.SimpleJobTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);
    public static final String REGISTRATION_SECOND_PAGE = "Registration Second Page";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Autowired
    private FormStepConfigurer formStepConfigurer;

    @Autowired
    private BacteriologyStepConfigurer bacteriologyStepConfigurer;

    @Autowired
    private ObjectFactory<SimpleJobTemplate> simpleJobTemplateFactory;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Autowired
    private MartJSONReader martJSONReader;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private MetaDataStepConfigurer metaDataStepConfigurer;

    @Autowired
    private ObjectFactory<EAVJobTemplate> eavJobTemplateFactory;

    @Autowired
    private ViewExecutor viewExecutor;

    @Autowired
    private ProcedureExecutor procedureExecutor;

    @Autowired
    private RspStepConfigurer rspStepConfigurer;

    @Autowired
    private OrderStepConfigurer orderStepConfigurer;

    @Autowired
    private DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Autowired
    private CustomCodesUploader customCodesUploader;

    @Autowired
    private RspViewDefinition rspViewDefinition;

    @Autowired
    private DispositionStepConfigurer dispositionStepConfigurer;

    @Override
    public void run(String... args) {
        List<JobDefinition> jobDefinitions = jobDefinitionReader.getJobDefinitions();
        if (!JobDefinitionValidator.validate(jobDefinitions))
            throw new InvalidJobConfiguration();
        launchJobs(getJobs(jobDefinitions));

        List<ProcedureDefinition> procedures = martJSONReader.getProcedureDefinitions();

        procedureExecutor.execute(procedures);

        List<ViewDefinition> viewDefinitions = martJSONReader.getViewDefinitions();

        if (!StringUtils.isEmpty(jobDefinitionReader.getJobDefinitionByName(REGISTRATION_SECOND_PAGE).getName())) {
            viewDefinitions.add(rspViewDefinition.getDefinition());
        }

        viewExecutor.execute(viewDefinitions);
    }

    private void launchJobs(List<Job> jobs) {
        jobs.forEach(job -> {
            try {
                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                jobParametersBuilder.addDate(job.getName(), new Date());
                jobLauncher.run(job, jobParametersBuilder.toJobParameters());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        });
    }

    private List<Job> getJobs(List<JobDefinition> jobDefinitions) {
        return jobDefinitions.stream().map(this::getJobByDefinition)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Job getJobByDefinition(JobDefinition jobDefinition) {
        try {
            return getJobByType(jobDefinition);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    private Job getJobByType(JobDefinition jobDefinition) throws Exception {
        switch (jobDefinition.getType()) {
          case "obs":
              return buildObsJob(jobDefinition);
          case "eav":
              return eavJobTemplateFactory.getObject().buildJob(jobDefinition);
          case "bacteriology":
              return buildBacteriologyJob(jobDefinition);
          case "metadata":
              return buildMetaDataJob(jobDefinition);
          case "orders":
              return buildOrdersJob(jobDefinition);
          case "diagnoses":
              return buildDiagnosesJob(jobDefinition);
          case "csvupload":
              return customCodesUploader.buildJob(jobDefinition);
          case "rsp":
              return buildRspJob(jobDefinition);
          case "disposition":
              return buildDispositionJob(jobDefinition);
          default:
              return simpleJobTemplateFactory.getObject().buildJob(jobDefinition);
        }
    }

    private Job buildDispositionJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, dispositionStepConfigurer);
    }

    private Job getFlowJob(JobDefinition jobDefinition, StepConfigurerContract stepConfigurer) {
        return getJob(getFlowBuilder(jobDefinition.getName()), stepConfigurer, jobDefinition);
    }

    private Job buildRspJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, rspStepConfigurer);
    }

    private Job buildObsJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, formStepConfigurer);
    }

    private Job buildBacteriologyJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, bacteriologyStepConfigurer);
    }

    private Job buildDiagnosesJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, diagnosesStepConfigurer);
    }

    private Job buildOrdersJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, orderStepConfigurer);
    }

    private Job buildMetaDataJob(JobDefinition jobDefinition) {
        return getFlowJob(jobDefinition, metaDataStepConfigurer);
    }

    private FlowBuilder<FlowJobBuilder> getFlowBuilder(String jobName) {
        //TODO: Have to remove treatmentRegistrationBaseExportStep from flow
        return jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .flow(treatmentRegistrationBaseExportStep.getStep());
    }

    private Job getJob(FlowBuilder<FlowJobBuilder> completeDataExport,
                       StepConfigurerContract stepConfigurerContract, JobDefinition jobDefinition) {
        stepConfigurerContract.registerSteps(completeDataExport, jobDefinition);
        stepConfigurerContract.createTables();

        return completeDataExport.end().build();
    }
}
