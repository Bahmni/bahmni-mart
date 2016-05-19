select obs_id as obsIds
from obs o
		INNER join concept c on o.concept_id = c.concept_id and is_set=1
where obs_group_id in (:parentObsIds);
