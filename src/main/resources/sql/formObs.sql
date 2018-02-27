SELECT
  o.encounter_id                   AS encounterId,
  o.person_id                      AS patientId,
  o.concept_id                     AS conceptId,
  o.obs_id                         AS id,
  coalesce(DATE_FORMAT(o.value_datetime, '%d/%b/%Y'), o.value_numeric, o.value_text, cvn.concept_full_name,
           cvn.concept_short_name) AS value,
  obs_con.concept_full_name        AS conceptName,
  parent_obs_con.concept_full_name AS parentConceptName
FROM obs o
  JOIN concept_view obs_con ON o.concept_id = obs_con.concept_id
  LEFT OUTER JOIN obs as parent_obs on parent_obs.obs_id = o.obs_group_id and parent_obs.voided = 0
  LEFT OUTER JOIN concept_view parent_obs_con ON parent_obs.concept_id = parent_obs_con.concept_id
  LEFT OUTER JOIN concept codedConcept ON o.value_coded = codedConcept.concept_id
  LEFT OUTER JOIN concept_view cvn ON codedConcept.concept_id = cvn.concept_id
WHERE
  o.obs_id = :obsId
  AND o.voided = 0


