package org.bahmni.mart.form;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class FormListProcessor extends AbstractFormListProcessor {

    @Autowired
    private BahmniFormFactory bahmniFormFactory;

    private static final Logger logger = LoggerFactory.getLogger(FormListProcessor.class);

    @Override
    public List<BahmniForm> retrieveAllForms(List<Concept> formConcepts, JobDefinition jobDefinition) {
        List<String> ignoreConceptNames = JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        List<BahmniForm> forms = formConcepts.stream()
                .filter(concept -> !ignoreConceptNames.contains(concept.getName()))
                .map(concept -> bahmniFormFactory.createForm(concept, null, jobDefinition))
                .collect(Collectors.toList());

        return getUniqueFlattenedBahmniForms(forms, logger);
    }
}
