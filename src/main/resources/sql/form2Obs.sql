SELECT
  o.encounter_id,
  o.person_id                                                                     AS patientId,
  o.concept_id                                                                    AS conceptId,
  o.obs_id                                                                        AS id,
  COALESCE(DATE_FORMAT(o.value_datetime, '%d/%b/%Y %T'), o.value_numeric, o.value_text,
           vcc.code, cvn.concept_full_name,
           cvn.concept_short_name)                                                AS value,
  COALESCE(obs_con.name)                                     AS conceptName,
  o.obs_datetime                                                                  AS obsDateTime,
  o.date_created                                                                  AS dateCreated,
  o.location_id                                                                   AS locationId,
  l.name                                                                          AS locationName,
  p.program_id                                                                    AS programId,
  p.name                                                                          AS programName,
  SUBSTRING(o.form_namespace_and_path, INSTR(o.form_namespace_and_path, '^') + 1) AS formFieldPath,
  e.visit_id                                                                      AS visit_id,
  pp.patient_program_id                                                           AS patient_program_id
FROM obs o
  JOIN concept_name obs_con ON o.form_namespace_and_path LIKE CONCAT('%', :formName, '.%')
                               AND (o.voided IS FALSE)
                               AND o.concept_id =
                                   obs_con.concept_id AND obs_con.name IN
                                                          (:conceptNames)
                               AND obs_con.concept_name_type = 'FULLY_SPECIFIED'
  LEFT OUTER JOIN location l ON o.location_id = l.location_id
  LEFT OUTER JOIN encounter e on o.encounter_id = e.encounter_id
  LEFT OUTER JOIN episode_encounter ee ON ee.encounter_id = o.encounter_id
  LEFT OUTER JOIN episode_patient_program epp ON ee.episode_id = epp.episode_id
  LEFT OUTER JOIN patient_program pp ON epp.patient_program_id = pp.patient_program_id
  LEFT OUTER JOIN program p ON pp.program_id = p.program_id
  LEFT OUTER JOIN concept codedConcept ON o.value_coded = codedConcept.concept_id
  LEFT OUTER JOIN concept_view cvn ON codedConcept.concept_id = cvn.concept_id
  LEFT OUTER JOIN concept_reference_term_map_view vcc ON vcc.concept_id = o.value_coded
                                                         AND vcc.concept_map_type_name = 'SAME-AS'
                                                         AND
                                                         vcc.concept_reference_source_name = :conceptReferenceSource
WHERE o.encounter_id = :encounterId
GROUP BY IFNULL(o.value_coded, UUID()), formFieldPath;
