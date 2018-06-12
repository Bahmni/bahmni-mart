package org.bahmni.mart.form;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.mart.form.domain.Obs;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class FormBuilderObservationProcessor extends AbstractObservationProcessor {
    @Override
    public List<Obs> process(Map<String, Object> obsRow) {
        List<Integer> allChildObsIds = getChildObsIds(obsRow);

        List<Obs> obsRows = fetchAllLeafObs(allChildObsIds, (Integer) obsRow.get("parent_obs_id"));
        obsRows.addAll(getFormObs(obsRow));
        setObsIdAndParentObsId(obsRows, (Integer) obsRow.get("obs_id"), (Integer) obsRow.get("parent_obs_id"));

        return obsRows;
    }

    private List<Integer> getChildObsIds(Map<String, Object> obsRow) {
        List<Integer> allChildObsIds = new ArrayList<>();
        Integer encounterId = (Integer) obsRow.get("encounter_id");

        List<Map<String, Object>> records = getObsRow(encounterId);
        ArrayList<Integer> obsGroupIds = new ArrayList<>();
        for (Map<String, Object> record : records) {
            Integer obsId = (Integer) record.get("obs_id");
            Boolean isConceptSet = (Boolean) record.get("is_concept_set");
            if (!isConceptSet)
                allChildObsIds.add(obsId);
            else
                obsGroupIds.add(obsId);
        }

        if (CollectionUtils.isNotEmpty(obsGroupIds))
            retrieveChildObsIds(allChildObsIds, obsGroupIds);
        return allChildObsIds;
    }

    private List<Map<String, Object>> getObsRow(Integer encounterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("encounterId", encounterId);

        return jdbcTemplate.queryForList("SELECT obs_id, c.concept_id, c.is_set AS is_concept_set FROM obs" +
                " INNER JOIN concept c ON obs.concept_id = c.concept_id WHERE encounter_id = :encounterId AND" +
                " obs_group_id IS NULL AND voided = FALSE", params);

    }


}
