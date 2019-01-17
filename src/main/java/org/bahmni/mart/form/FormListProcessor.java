package org.bahmni.mart.form;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.helper.FormListHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class FormListProcessor {

    @Autowired
    private BahmniFormFactory bahmniFormFactory;
    private static final Logger logger = LoggerFactory.getLogger(FormListProcessor.class);

    public List<BahmniForm> retrieveAllForms(List<Concept> formConcepts, JobDefinition jobDefinition) {
        List<String> ignoreConceptNames = JobDefinitionUtil.getIgnoreConceptNamesForJob(jobDefinition);
        List<BahmniForm> forms = formConcepts.stream()
                .filter(concept -> !ignoreConceptNames.contains(concept.getName()))
                .map(concept -> bahmniFormFactory.createForm(concept, null, jobDefinition))
                .collect(Collectors.toList());
        List<BahmniForm> flattenedFormList = FormListHelper.flattenFormList(forms);
        return FormListHelper.filterFormsWithOutDuplicateConcepts(flattenedFormList);
    }




}
