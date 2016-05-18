package org.bahmni.batch;

import org.bahmni.batch.exports.NonTBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.ObservationExportStep;
import org.bahmni.batch.exports.PatientRegistrationBaseExportStep;
import org.bahmni.batch.exports.TBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.batch.form.BahmniFormFactory;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.ObsService;
import org.bahmni.batch.observation.FormListProcessor;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {

    public static final String FILE_NAME_EXTENSION=".csv";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private PatientRegistrationBaseExportStep patientRegistrationBaseExportStep;

    @Autowired
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Autowired
    private TBDrugOrderBaseExportStep tbDrugOrderBaseExportStep;

    @Autowired
    private NonTBDrugOrderBaseExportStep nonTBDrugOrderBaseExportStep;

    @Autowired
    private FormListProcessor formListProcessor;

    @Autowired
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Value("${outputFolder}")
    public String outputFolder;

    @Autowired
    public JobCompletionNotificationListener jobCompletionNotificationListener;

    @Autowired
    private BahmniFormFactory bahmniFormFactory;



    @Bean
    public JobExecutionListener listener() {
        return jobCompletionNotificationListener;
    }


    @Bean
    public Job completeDataExport() throws URISyntaxException {

        List<BahmniForm> forms = formListProcessor.retrieveForms();
        FlowBuilder<FlowJobBuilder> completeDataExport = jobBuilderFactory.get("completeDataExport")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(patientRegistrationBaseExportStep.getStep());
//                .next(treatmentRegistrationBaseExportStep.getStep())
//                .next(tbDrugOrderBaseExportStep.getStep())
//                .next(nonTBDrugOrderBaseExportStep.getStep());

        for(BahmniForm form: forms){
                ObservationExportStep observationExportStep = observationExportStepFactory.getObject();
                observationExportStep.setForm(form);
            String fileName = form.getFormName().getName().replaceAll("\\s","")+FILE_NAME_EXTENSION;
            observationExportStep.setOutputFolder(new FileSystemResource(new URI(outputFolder).getSchemeSpecificPart()+File.separator+fileName));
                completeDataExport = completeDataExport.next(observationExportStep.getStep());
        }

        return completeDataExport
                .end()
                .build();
    }




}
