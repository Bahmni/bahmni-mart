SELECT
  cn.concept_id AS id,
  cn.name,
  c.is_set      AS isSet
FROM concept_name cn
  INNER JOIN concept c ON cn.concept_id = c.concept_id
WHERE cn.name IN (:conceptNames) AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0;
