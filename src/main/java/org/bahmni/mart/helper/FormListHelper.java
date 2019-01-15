package org.bahmni.mart.helper;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FormListHelper {
    private static final Logger logger = LoggerFactory.getLogger(FormListHelper.class);

    public static List<BahmniForm> filterFormsWithOutDuplicateConcepts(List<BahmniForm> bahmniForms) {
        return bahmniForms.stream().filter(bahmniForm -> !hasDuplicateConcepts(bahmniForm))
                .collect(Collectors.toList());
    }

    private static boolean hasDuplicateConcepts(BahmniForm bahmniForm) {
        if (bahmniForm != null) {
            Set<String> concepts = new HashSet<>();
            for (Concept concept : bahmniForm.getFields()) {
                if (!concepts.add(concept.getName())) {
                    logger.warn(String.format("Skipping the form '%s' since it has duplicate concepts '%s'",
                            bahmniForm.getFormName().getName(), concept.getName()));
                    return true;
                }
            }
        }
        return false;
    }

    public static  List<BahmniForm> flattenFormList(List<BahmniForm> forms) {
        List<BahmniForm> flattenedList = new ArrayList<>();
         flattenAllForms(forms, flattenedList);
         return flattenedList;
    }

    private static void flattenAllForms(List<BahmniForm> forms, List<BahmniForm> flattenedList) {
        for (BahmniForm form : forms) {
            List<BahmniForm> children = form.getChildren();
            if (!children.isEmpty()) {
                flattenedList.addAll(children);
                flattenAllForms(children, flattenedList);
            }
        }
    }

}
