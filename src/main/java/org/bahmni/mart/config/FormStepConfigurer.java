package org.bahmni.mart.config;

import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FormStepConfigurer implements StepConfigurer {

    @Autowired
    private TableGeneratorStep tableGeneratorStep;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private FormListProcessor formListProcessor;

    @Autowired
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Override
    public void createTables() {
        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList());
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport) {
        List<BahmniForm> forms = formListProcessor.retrieveAllForms();
        for (BahmniForm form : forms) {
            ObservationExportStep observationExportStep = observationExportStepFactory.getObject();
            observationExportStep.setForm(form);
            completeDataExport.next(observationExportStep.getStep());
            formTableMetadataGenerator.addMetadataForForm(form);
        }
    }
}
