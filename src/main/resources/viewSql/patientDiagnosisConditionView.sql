SELECT
  pd.person_id AS patient_id,
  pd.gender,
  pd.birthyear               AS birth_year,
  EXTRACT(YEAR FROM (SELECT age( c.onset_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')))) AS age_at_condition,
  age_group(c.onset_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')) AS age_group_at_condition,
  pd.dead,
  pa.*,
  c.condition_id,
  c.previous_condition_id,
  c.condition_name,
  c.status,
  c.onset_date as condition_onset_date,
  c.end_date AS condition_end_date,
  c.end_reason,
  c.date_created AS condition_date_created,
  c.creator_name AS creator,
  vd.encounter_id,
  vd.coded_diagnosis,
  vd.non_coded_diagnosis,
  vd.diagnosis_certainty,
  vd.diagnosis_order,
  vd.obs_datetime

FROM person_details_default pd
  LEFT JOIN person_attributes pa ON pa.person_id = pd.person_id
  LEFT JOIN conditions_default c ON c.patient_id = pd.person_id
  LEFT JOIN visit_diagnoses vd ON vd.patient_id = pd.person_id
