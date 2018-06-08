SELECT
  pd.person_id               AS patient_id,
  pi."Patient_Identifier"    AS patient_identifier,
  pd.gender,
  pd.birthdate               AS birth_date,
  pd.dead,
  pa.*,
  pap.appointment_id,
  pap.appointment_provider,
  pap.appointment_start_time,
  pap.appointment_end_time,
  pap.appointment_speciality,
  pap.appointment_service,
  pap.appointment_service_type,
  pap.appointment_status,
  pap.appointment_location,
  pap.appointment_kind
FROM person_details pd
  LEFT JOIN person_attributes pa ON pa.person_id = pd.person_id
  LEFT JOIN patient_identifier pi ON pi.patient_id = pd.person_id
  LEFT JOIN patient_appointment pap ON pap.patient_id = pa.person_id