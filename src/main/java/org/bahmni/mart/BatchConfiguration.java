package org.bahmni.mart;

import freemarker.template.TemplateExceptionHandler;
import org.bahmni.mart.config.FormStepConfigurer;
import org.bahmni.mart.config.ProgramDataStepConfigurer;
import org.bahmni.mart.config.StepConfigurer;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {

    public static final String FULL_DATA_EXPORT_JOB_NAME = "ammanExports";
    private static final String DEFAULT_ENCODING = "UTF-8";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Autowired
    private FormStepConfigurer formStepConfigurer;

    @Autowired
    private ProgramDataStepConfigurer programDataStepConfigurer;

    private List<StepConfigurer> stepConfigurers = new ArrayList<>();

    @Bean
    public Job completeDataExport() throws IOException {
        FlowBuilder<FlowJobBuilder> completeDataExport = jobBuilderFactory.get(FULL_DATA_EXPORT_JOB_NAME)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .flow(treatmentRegistrationBaseExportStep.getStep());
        //TODO: Have to remove treatmentRegistrationBaseExportStep from flow

        setStepConfigurers();

        for (StepConfigurer stepConfigurer : stepConfigurers) {
            stepConfigurer.registerSteps(completeDataExport);
            stepConfigurer.createTables();
        }
        return completeDataExport.end().build();
    }

    private void setStepConfigurers() {
        stepConfigurers.add(formStepConfigurer);
        stepConfigurers.add(programDataStepConfigurer);
    }


    @Bean
    public freemarker.template.Configuration freeMarkerConfiguration() throws IOException {
        freemarker.template.Configuration freemarkerTemplateConfig = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_22);
        freemarkerTemplateConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerTemplateConfig.setDefaultEncoding(DEFAULT_ENCODING);
        freemarkerTemplateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return freemarkerTemplateConfig;
    }

    @PreDestroy
    public void generateReport() {

    }
}
