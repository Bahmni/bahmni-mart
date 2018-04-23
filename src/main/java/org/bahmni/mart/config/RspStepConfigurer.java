package org.bahmni.mart.config;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.mart.config.job.JobDefinitionUtil.getIgnoreConceptNamesForJob;
import static org.bahmni.mart.config.job.JobDefinitionUtil.getJobDefinitionByType;

@Component
public class RspStepConfigurer extends StepConfigurer {
    private static final String TYPE = "rsp";

    @Override
    protected List<BahmniForm> getAllForms() {
        List<String> ignoreConcepts = getIgnoreConceptNamesForJob(
                getJobDefinitionByType(jobDefinitionReader.getJobDefinitions(), TYPE));

        List<Concept> allFormConcepts = obsService.getConceptsByNames(
                Arrays.asList("Nutritional Values", "Fee Information"));
        return addPrefixToFormsName(formListProcessor.retrieveAllForms(allFormConcepts, ignoreConcepts));
    }

    private List<BahmniForm> addPrefixToFormsName(List<BahmniForm> bahmniForms) {
        return bahmniForms.stream().peek(bahmniForm -> {
            Concept formName = bahmniForm.getFormName();
            formName.setName(FormTableMetadataGenerator.addPrefixToName(formName.getName(), TYPE));
            bahmniForm.setFormName(formName);
        }).collect(Collectors.toList());
    }
}
