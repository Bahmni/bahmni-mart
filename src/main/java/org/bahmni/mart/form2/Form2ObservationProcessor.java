package org.bahmni.mart.form2;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Component
@Scope(value = "prototype")
public class Form2ObservationProcessor implements ItemProcessor<Map<String, Object>, List<Obs>> {

    private static final String SLASH = "/";
    private static final String DOT = ".";
    private BahmniForm form;

    @Override
    public List<Obs> process(Map<String, Object> item) {
        Obs obs = new Obs();
        obs.setEncounterId(String.valueOf(item.get("encounter_id")));
        obs.setPatientId(String.valueOf(item.get("patientId")));
        obs.setId((Integer) item.get("id"));
        Concept concept = new Concept();
        concept.setId((Integer) item.get("conceptId"));
        concept.setName(this.form.getFieldNameAndFullySpecifiedNameMap().get((String) item.get("conceptName")));
        obs.setField(concept);
        String itemValue = (String) item.get("value");
        String value = this.form.getFieldNameAndFullySpecifiedNameMap().containsKey(itemValue) ?
                this.form.getFieldNameAndFullySpecifiedNameMap().get(itemValue) : itemValue;
        obs.setValue(value);
        obs.setObsDateTime(String.valueOf(item.get("obsDateTime")));
        obs.setDateCreated(String.valueOf(item.get("dateCreated")));
        obs.setLocationId(String.valueOf(item.get("locationId")));
        obs.setLocationName((String) item.get("locationName"));
        obs.setProgramId(String.valueOf(item.get("programId")));
        obs.setProgramName((String) item.get("programName"));
        String formFieldPath = (String) item.get("formFieldPath");
        setFormFieldPath(obs, formFieldPath);
        return singletonList(obs);
    }

    private void setFormFieldPath(Obs obs, String formFieldPath) {
        int formDepthToParent = form.getDepthToParent();
        obs.setFormFieldPath(getProcessedFormFieldPath(formFieldPath, formDepthToParent));
        if (formDepthToParent != 0) {
            int parentFormDepthToParent = form.getParent().getDepthToParent();
            obs.setReferenceFormFieldPath(getProcessedFormFieldPath(formFieldPath, parentFormDepthToParent));
        }
    }

    private String getProcessedFormFieldPath(String formFieldPath, int depthToParent) {
        if (depthToParent == 0) {
            return getFormNameFrom(formFieldPath);
        }
        int slashIndex = getSlashIndex(formFieldPath, depthToParent);
        return StringUtils.substring(formFieldPath, 0, slashIndex);
    }

    private String getFormNameFrom(String formFieldPath) {
        return StringUtils.substringBefore(formFieldPath, DOT);
    }

    private int getSlashIndex(String formFieldPath, int depthToParent) {
        int slashCount = 0;
        int slashIndex = -1;
        while (slashCount <= depthToParent) {
            slashIndex = formFieldPath.indexOf(SLASH, slashIndex + 1);
            slashCount++;
        }
        return slashIndex == -1 ? formFieldPath.length() : slashIndex;
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }
}
