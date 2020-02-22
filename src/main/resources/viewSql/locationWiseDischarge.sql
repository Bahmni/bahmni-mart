SELECT
  bpam.patient_id,
  bpam.visit_id,
  bpam.location,
  min(bpam.date_started)              AS start_date,
  coalesce(discharge_date(patient_id,bpam.location,date_stopped),null) AS discharge_date
FROM bed_patient_assignment_default bpam
GROUP BY patient_id,visit_id,discharge_date,location
ORDER BY start_date,discharge_date
ORDER BY start_date,discharge_date
