SELECT
  obs_id   AS obsId,
  c.is_set AS isSet
FROM obs o
  INNER JOIN concept c ON o.concept_id = c.concept_id
WHERE obs_group_id IN (:parentObsIds)
      AND o.voided = 0;
