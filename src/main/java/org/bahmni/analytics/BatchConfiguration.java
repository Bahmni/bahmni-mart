package org.bahmni.analytics;

import freemarker.template.TemplateExceptionHandler;
import org.bahmni.analytics.exports.TreatmentRegistrationBaseExportStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {

    public static final String FULL_DATA_EXPORT_JOB_NAME = "ammanExports";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Value("${templates}")
    private Resource freemarkerTemplateLocation;

    @Autowired
    public JobCompletionNotificationListener jobCompletionNotificationListener;

    private static final String DEFAULT_ENCODING = "UTF-8";

    @Bean
    public JobExecutionListener listener() {
        return jobCompletionNotificationListener;
    }

    @Bean
    public Job completeDataExport() throws IOException {
        FlowBuilder<FlowJobBuilder> completeDataExport = jobBuilderFactory.get(FULL_DATA_EXPORT_JOB_NAME)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .listener(listener())
                .flow(treatmentRegistrationBaseExportStep.getStep());

        return completeDataExport.end().build();
    }


    @Bean
    public freemarker.template.Configuration freeMarkerConfiguration() throws IOException {
        freemarker.template.Configuration freemarkerTemplateConfig = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_22);
        freemarkerTemplateConfig.setDirectoryForTemplateLoading(freemarkerTemplateLocation.getFile());

        freemarkerTemplateConfig.setDefaultEncoding(DEFAULT_ENCODING);
        freemarkerTemplateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return freemarkerTemplateConfig;
    }

    @PreDestroy
    public void generateReport() {

    }
}
