SELECT
  pi.*,
  pd.gender,
  pd.birthdate               AS birth_date,
  pd.dead,
  pa.*,
  bpa.visit_id,
  bpa.location,
  bpa.date_started           AS bed_assigned_date,
  bpa.date_stopped           AS bed_discharged_date
FROM person_details pd
  LEFT JOIN person_attributes pa ON pa.person_id = pd.person_id
LEFT JOIN bed_patient_assignment bpa ON bpa.patient_id = pd.person_id
LEFT JOIN patient_identifier pi ON pi.patient_id = pd.person_id