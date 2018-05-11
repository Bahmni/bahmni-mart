package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.service.ConceptService;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class StepConfigurer implements StepConfigurerContract {

    @Autowired
    protected TableGeneratorStep tableGeneratorStep;

    @Autowired
    protected FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    protected ObjectFactory<ObservationExportStep> observationExportStepFactory;


    @Autowired
    protected FormListProcessor formListProcessor;

    @Autowired
    protected JobDefinitionReader jobDefinitionReader;

    @Autowired
    protected ConceptService conceptService;

    @Override
    public void createTables() {
        tableGeneratorStep.createTables(formTableMetadataGenerator.getTableDataList());
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition) {
        List<BahmniForm> forms = getAllForms();
        for (BahmniForm form : forms) {
            ObservationExportStep observationExportStep = observationExportStepFactory.getObject();
            observationExportStep.setJobDefinition(jobDefinition);
            observationExportStep.setForm(form);
            completeDataExport.next(observationExportStep.getStep());
            formTableMetadataGenerator.addMetadataForForm(form);
        }
    }

    protected abstract List<BahmniForm> getAllForms();
}
