select obs_id as obsId, c.is_set as isSet
from obs o
		INNER join concept c on o.concept_id = c.concept_id
where obs_group_id in (:parentObsIds)
and o.voided=0;
