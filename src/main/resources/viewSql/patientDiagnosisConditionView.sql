WITH all_diagnoses AS (
  SELECT
    patient_id,
    encounter_id,
    coded_diagnosis,
    non_coded_diagnosis,
    diagnosis_certainty,
    diagnosis_order,
    obs_datetime AS diagnosis_date,
    'legacy' AS diagnosis_source
  FROM visit_diagnoses

  UNION ALL

  SELECT
    patient_id,
    encounter_id,
    coded_diagnosis,
    non_coded_diagnosis,
    CASE diagnosis_certainty
      WHEN 'CONFIRMED' THEN 'Confirmed'
      WHEN 'PROVISIONAL' THEN 'Provisional'
      ELSE diagnosis_certainty
    END AS diagnosis_certainty,
    CASE diagnosis_order
      WHEN 1 THEN 'Primary'
      WHEN 2 THEN 'Secondary'
      ELSE diagnosis_order::text
    END AS diagnosis_order,
    date_created AS diagnosis_date,
    'new_ui' AS diagnosis_source
  FROM encounter_diagnosis_default
)

SELECT
  pd.person_id AS patient_id,
  pd.gender,
  pd.birthyear               AS birth_year,
  EXTRACT(YEAR FROM (SELECT age( c.onset_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')))) AS age_at_condition,
  age_group(c.onset_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')) AS age_group_at_condition,
  pd.dead,
  pa.*,
  c.condition_id,
  c.previous_version,
  c.condition_name,
  c.clinical_status,
  c.onset_date as condition_onset_date,
  c.end_date AS condition_end_date,
  c.date_created AS condition_date_created,
  c.creator_name AS creator,
  d.encounter_id,
  d.coded_diagnosis,
  d.non_coded_diagnosis,
  d.diagnosis_certainty,
  d.diagnosis_order,
  d.diagnosis_date,
  d.diagnosis_source

FROM person_details_default pd
  LEFT JOIN person_attributes pa ON pa.person_id = pd.person_id
  LEFT JOIN conditions_default c ON c.patient_id = pd.person_id
  LEFT JOIN all_diagnoses d ON d.patient_id = pd.person_id
