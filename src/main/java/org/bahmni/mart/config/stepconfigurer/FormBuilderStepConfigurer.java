package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.form.FormBuilderFormListProcessor;
//import org.bahmni.mart.exports.AbstractObservationExportStep;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exports.FormBuilderObservationExportStep;
import org.bahmni.mart.form.domain.BahmniForm;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bahmni.mart.config.job.JobDefinitionUtil.isAddMoreMultiSelectEnabled;

@Component
@Scope(value = "prototype")
public class FormBuilderStepConfigurer extends StepConfigurer {

    @Autowired
    private FormBuilderFormListProcessor formBuilderFormListProcessor;

    @Autowired
    protected ObjectFactory<FormBuilderObservationExportStep> observationExportStepFactory;

    @Override
    protected List<BahmniForm> getAllForms() {
        return formBuilderFormListProcessor.getAllForms();
    }

    @Override
    public void registerSteps(FlowBuilder<FlowJobBuilder> completeDataExport, JobDefinition jobDefinition) {
        List<BahmniForm> forms = getAllForms();
        for (BahmniForm form : forms) {
            FormBuilderObservationExportStep observationExportStep = observationExportStepFactory.getObject();
            observationExportStep.setJobDefinition(jobDefinition);
            observationExportStep.setForm(form);
            completeDataExport.next(observationExportStep.getStep());
            formTableMetadataGenerator.addMetadataForForm(form);

            if (!isAddMoreMultiSelectEnabled(jobDefinition)) {
                revokeConstraints(formTableMetadataGenerator.getTableData(form));
            }
        }
    }

}
