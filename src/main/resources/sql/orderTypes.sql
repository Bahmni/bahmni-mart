SELECT
  om.order_type_id
FROM concept_set cs
  INNER JOIN concept c ON c.concept_id = cs.concept_id AND cs.concept_set IN (:sampleConceptIds)
  INNER JOIN concept_class cc ON cc.concept_class_id = c.class_id
  INNER JOIN order_type_class_map om ON om.concept_class_id = cc.concept_class_id;
