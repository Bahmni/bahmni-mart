SELECT
  pd.person_id,
  pd.gender,
  pd.birthyear,
  EXTRACT(YEAR FROM (SELECT age( pvd.visit_start_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')))) AS age_at_visit,
  age_group(pvd.visit_start_date, TO_DATE(CONCAT('01-01-', pd.birthyear), 'dd-MM-yyyy')) AS age_group_at_visit,
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

FROM person_details_default pd INNER JOIN patient_visit_details_default pvd ON pvd.patient_id = pd.person_id
                             INNER JOIN patient_encounter_details_default ped ON ped.patient_id = pvd.patient_id
                             AND ped.visit_id = pvd.visit_id;
