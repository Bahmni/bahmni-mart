SELECT DISTINCT
  childConcept.concept_id                                        AS id,
  childConcept.concept_full_name                                 AS name,
  childConceptRaw.is_set                                         AS isset,
  COALESCE(cv.code, cn.concept_full_name, cn.concept_short_name) AS title
FROM concept_view parentConcept
  INNER JOIN concept_set cs ON parentConcept.concept_id = cs.concept_set
  INNER JOIN concept_view childConcept ON cs.concept_id = childConcept.concept_id
  INNER JOIN concept childConceptRaw ON childConcept.concept_id = childConceptRaw.concept_id
  LEFT OUTER JOIN concept_reference_term_map_view cv
    ON (cv.concept_id = childConceptRaw.concept_id AND cv.concept_map_type_name = 'SAME-AS' AND
        cv.concept_reference_source_name = 'MSF-INTERNAL')
  LEFT OUTER JOIN concept_view cn ON (cn.concept_id = childConceptRaw.concept_id)
WHERE parentConcept.concept_full_name = :parentConceptName AND childConcept.retired = 0
ORDER BY cs.sort_weight;