package org.bahmni.mart.form;

import org.bahmni.mart.form.domain.Obs;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class ObservationProcessor extends AbstractObservationProcessor {

    @Override
    public List<Obs> process(Map<String, Object> obsRow) {
        List<Integer> allChildObsIds = new ArrayList<>();

        if (form.getFormName().getIsSet() == 1) {
            retrieveChildObsIds(allChildObsIds, Arrays.asList((Integer) obsRow.get("obs_id")));
        } else {
            allChildObsIds.add((Integer) obsRow.get("obs_id"));
        }

        List<Obs> obsRows = fetchAllLeafObs(allChildObsIds,(Integer) obsRow.get("parent_obs_id"));
        obsRows.addAll(getFormObs(obsRow));
        setObsIdAndParentObsId(obsRows, (Integer) obsRow.get("obs_id"), (Integer) obsRow.get("parent_obs_id"));

        return obsRows;
    }
}
