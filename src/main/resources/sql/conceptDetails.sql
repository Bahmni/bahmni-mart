SELECT
  cn.concept_id AS id,
  cn.name,
  c.is_set      AS isSet,
  cdt.name      AS dataType
FROM concept_name cn
  INNER JOIN concept c ON cn.concept_id = c.concept_id
  INNER JOIN concept_datatype cdt ON c.datatype_id = cdt.concept_datatype_id AND cdt.retired IS FALSE
WHERE cn.name IN (:conceptNames) AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0;
