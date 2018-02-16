SELECT
  o.person_id                      AS treatmentNumber,
  o.concept_id                     AS conceptId,
  o.obs_id                         AS id,
  coalesce(DATE_FORMAT(o.value_datetime, '%d/%b/%Y'), o.value_numeric, o.value_text, cv.code, cvn.concept_full_name,
           cvn.concept_short_name) AS value,
  obs_con.concept_full_name        AS conceptName
FROM patient_program pp
  JOIN episode_patient_program epp ON pp.patient_program_id = epp.patient_program_id
  JOIN episode_encounter ep_en ON epp.episode_id = ep_en.episode_id
  JOIN obs o ON ep_en.encounter_id = o.encounter_id
  JOIN concept_view obs_con ON o.concept_id = obs_con.concept_id
  LEFT OUTER JOIN concept codedConcept ON o.value_coded = codedConcept.concept_id
  LEFT OUTER JOIN concept_reference_term_map_view cv
    ON cv.concept_id = codedConcept.concept_id AND cv.concept_map_type_name = 'SAME-AS' AND
       cv.concept_reference_source_name = 'MSF-INTERNAL'
  LEFT OUTER JOIN concept_view cvn ON codedConcept.concept_id = cvn.concept_id
WHERE
  o.obs_id IN (:childObsIds)
  AND obs_con.concept_id IN (:leafConceptIds)
  AND o.voided = 0
  AND pp.voided = 0;
