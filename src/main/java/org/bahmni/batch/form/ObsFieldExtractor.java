package org.bahmni.batch.form;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.domain.Obs;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObsFieldExtractor implements FieldExtractor<List<Obs>> {

    private BahmniForm form;

    public ObsFieldExtractor(BahmniForm form) {
        this.form = form;
    }

    @Override
    public Object[] extract(List<Obs> obsList) {
        List<Object> row = new ArrayList<>();

        if (obsList.isEmpty())
            return row.toArray();

        Map<Concept, String> obsRow = new HashMap<>();
        obsList.forEach(obs -> obsRow.put(obs.getField(),obs.getValue()));

        Obs firstObs = obsList.get(0);
        row.add(firstObs.getId());

        if (form.getParent() != null)
            row.add(firstObs.getParentId());

        row.add(firstObs.getTreatmentNumber());
        form.getFields().forEach(field -> row.add(formatObsValue(obsRow.get(field))));

        return row.toArray();
    }

    private String formatObsValue(String text) {
        return StringUtils.isEmpty(text) ? text : text.replaceAll("[\n\t,]", " ");
    }
}
