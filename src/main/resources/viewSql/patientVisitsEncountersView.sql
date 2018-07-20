SELECT
  pd.person_id,
  pd.gender,
  pd.birthtime,
  pd.birthdate_estimated,
  pvd.visit_id,
  pvd.visit_type_name,
  pvd.visit_start_date,
  pvd.visit_end_date,
  pvd.location_name,
  visit_type_name AS visit_type,
  encounter_id,
  encounter_type_id,
  encounter_type_name,
  ped.location_name AS encounter_location

FROM person_details_default pd LEFT OUTER JOIN patient_visit_details_default pvd ON pvd.patient_id = pd.person_id
LEFT OUTER JOIN patient_encounter_details_default ped ON ped.patient_id = pvd.patient_id