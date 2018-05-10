package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.RspConfigHelper;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Component
public class RspStepConfigurer extends StepConfigurer {
    private static final String TYPE = "rsp";

    @Autowired
    private RspConfigHelper rspConfigHelper;

    @Override
    protected List<BahmniForm> getAllForms() {
        JobDefinition jobDefinition = getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), TYPE);

        List<String> rspConcepts = rspConfigHelper.getRspConcepts();
        if (rspConcepts.isEmpty())
            return new ArrayList<>();

        List<Concept> allFormConcepts = obsService.getConceptsByNames(rspConcepts);
        return addPrefixToFormsName(formListProcessor.retrieveAllForms(allFormConcepts, jobDefinition));
    }

    private List<BahmniForm> addPrefixToFormsName(List<BahmniForm> bahmniForms) {
        return bahmniForms.stream().peek(bahmniForm -> {
            Concept formName = bahmniForm.getFormName();
            formName.setName(FormTableMetadataGenerator.addPrefixToName(formName.getName(), TYPE));
            bahmniForm.setFormName(formName);
        }).collect(Collectors.toList());
    }
}
