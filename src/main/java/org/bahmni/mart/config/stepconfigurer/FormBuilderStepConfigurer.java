package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.FormBuilderProcessor;
//import org.bahmni.mart.exports.AbstractObservationExportStep;
import org.bahmni.mart.form.domain.BahmniForm;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(value = "prototype")
public class FormBuilderStepConfigurer extends StepConfigurer {
//    @Autowired
//    private ObjectFactory<AbstractObservationExportStep> observationExportStepFactory;

//    @Autowired
//    private ObjectFactory<FormBuilderObservationExportStep> formBuilderObservationExportStepObjectFactory;

    @Autowired
    private FormBuilderProcessor formBuilderProcessor;

    @Override
    protected List<BahmniForm> getAllForms() {
        return formBuilderProcessor.getAllForms();
    }

//    @Override
//    protected ObjectFactory<AbstractObservationExportStep> getExportStep(){
//        return observationExportStepFactory;
//    }

}
