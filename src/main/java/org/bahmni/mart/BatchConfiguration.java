package org.bahmni.mart;

import freemarker.template.TemplateExceptionHandler;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {

    public static final String FULL_DATA_EXPORT_JOB_NAME = "ammanExports";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Autowired
    private FormListProcessor formListProcessor;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    private static final String DEFAULT_ENCODING = "UTF-8";

    @Bean
    public Job completeDataExport() throws IOException {
        List<BahmniForm> forms = formListProcessor.retrieveAllForms();

        FlowBuilder<FlowJobBuilder> completeDataExport = jobBuilderFactory.get(FULL_DATA_EXPORT_JOB_NAME)
                .incrementer(new RunIdIncrementer()).preventRestart()
                .flow(treatmentRegistrationBaseExportStep.getStep());
        //TODO: Have to remove treatmentRegistrationBaseExportStep from flow

        for (BahmniForm form : forms) {
            ObservationExportStep observationExportStep = observationExportStepFactory.getObject();
            observationExportStep.setForm(form);
            completeDataExport.next(observationExportStep.getStep());
            formTableMetadataGenerator.addMetadataForForm(form);
        }

        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList());

        return completeDataExport.end().build();
    }


    @Bean
    public freemarker.template.Configuration freeMarkerConfiguration() throws IOException {
        freemarker.template.Configuration freemarkerTemplateConfig = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_22);
        freemarkerTemplateConfig.setClassForTemplateLoading(this.getClass(),"/templates");
        freemarkerTemplateConfig.setDefaultEncoding(DEFAULT_ENCODING);
        freemarkerTemplateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return freemarkerTemplateConfig;
    }

    @PreDestroy
    public void generateReport() {

    }
}
