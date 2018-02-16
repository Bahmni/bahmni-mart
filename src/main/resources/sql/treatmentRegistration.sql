SELECT
  p.patient_id                                     AS patient_id,
  ps.patient_program_id                            AS `Patient_prg_id`,
  DATE_FORMAT(person.birthdate, '%d/%m/%Y')        AS dob,
  TIMESTAMPDIFF(YEAR, person.birthdate, CURDATE()) AS age,
  person.gender                                    AS sex,
  program.name                                     AS `Prg_name`,
  DATE_FORMAT(pp.date_enrolled, '%d/%m/%Y')        AS `Prg_start_dt`,
  DATE_FORMAT(pp.date_completed, '%d/%m/%Y')       AS `Prg_end_dt`,
  outcomeConcept.name                              AS `Prg_outcome`,
  programStateConcept.name                         AS `Prg_state`,
  DATE_FORMAT(ps.start_date, '%d/%m/%Y')           AS `Prg_state_start`,
  DATE_FORMAT(ps.end_date, '%d/%m/%Y')             AS `Prg_state_end`
FROM patient p
  JOIN person ON p.patient_id = person.person_id AND person.voided IS FALSE AND p.voided IS FALSE
  JOIN patient_program pp ON p.patient_id = pp.patient_id AND pp.voided IS FALSE
  JOIN program ON pp.program_id = program.program_id AND program.retired IS FALSE
  JOIN patient_state ps ON pp.patient_program_id = ps.patient_program_id AND ps.voided IS FALSE
  JOIN program_workflow_state pws
    ON ps.state = pws.program_workflow_state_id AND ps.voided IS FALSE AND pws.retired IS FALSE
  JOIN concept_name programStateConcept
    ON pws.concept_id = programStateConcept.concept_id AND programStateConcept.voided IS FALSE AND
       programStateConcept.concept_name_type = 'FULLY_SPECIFIED'
  LEFT JOIN concept_name outcomeConcept
    ON pp.outcome_concept_id = outcomeConcept.concept_id AND outcomeConcept.voided IS FALSE AND
       outcomeConcept.concept_name_type = 'FULLY_SPECIFIED'
ORDER BY pp.patient_program_id