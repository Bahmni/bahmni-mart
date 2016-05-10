package org.bahmni.batch;

import org.bahmni.batch.exports.NonTBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.PatientRegistrationBaseExportStep;
import org.bahmni.batch.exports.TBDrugOrderBaseExportStep;
import org.bahmni.batch.exports.TreatmentRegistrationBaseExportStep;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Autowired
    public PatientRegistrationBaseExportStep patientRegistrationBaseExportStep;

    @Autowired
    public TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Autowired
    public TBDrugOrderBaseExportStep tbDrugOrderBaseExportStep;

    @Autowired
    public NonTBDrugOrderBaseExportStep nonTBDrugOrderBaseExportStep;

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener();
    }
    @Bean
    public Job completeDataExport() {

        return jobBuilderFactory.get("completeDataExport")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(patientRegistrationBaseExportStep.getStep())
                .next(treatmentRegistrationBaseExportStep.getStep())
                .next(tbDrugOrderBaseExportStep.getStep())
                .next(nonTBDrugOrderBaseExportStep.getStep())
                .end()
                .build();
    }


}
