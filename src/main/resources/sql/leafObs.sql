SELECT
  o.encounter_id                   AS encounterId,
  o.person_id                      AS patientId,
  o.concept_id                     AS conceptId,
  o.obs_id                         AS id,
  coalesce(DATE_FORMAT(o.value_datetime, '%d/%b/%Y'), o.value_numeric, o.value_text, vcc.code, cvn.concept_full_name,
           cvn.concept_short_name) AS value,
  obs_con.concept_full_name        AS conceptName,
  parent_obs_con.concept_full_name AS parentConceptName
FROM  obs o
  JOIN concept_view obs_con ON o.concept_id = obs_con.concept_id
  LEFT OUTER JOIN obs as form_obs on form_obs.obs_id = o.obs_group_id and form_obs.voided = 0
  LEFT OUTER JOIN obs as parent_obs on parent_obs.obs_id = form_obs.obs_group_id and form_obs.voided = 0
  LEFT OUTER JOIN concept_view parent_obs_con ON parent_obs.concept_id = parent_obs_con.concept_id
  LEFT OUTER JOIN concept codedConcept ON o.value_coded = codedConcept.concept_id
  LEFT OUTER JOIN concept_view cvn ON codedConcept.concept_id = cvn.concept_id
  LEFT OUTER JOIN concept_reference_term_map_view vcc ON vcc.concept_id = o.value_coded
                                                         AND vcc.concept_map_type_name = 'SAME-AS'
                                                         AND vcc.concept_reference_source_name = :conceptReferenceSource
WHERE
  o.obs_id IN (:childObsIds)
  AND obs_con.concept_id IN (:leafConceptIds)
  AND o.voided = 0
