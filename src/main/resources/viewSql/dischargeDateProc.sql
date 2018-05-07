CREATE OR REPLACE FUNCTION discharge_date(patientId INT, locationName TEXT, dateStopped TIMESTAMP)
RETURNS TIMESTAMP AS $$
DECLARE
current_patient INT;
curr_start_date TIMESTAMP;
curr_stop_date TIMESTAMP;
BEGIN
SELECT patient_id,
  date_started,date_stopped
INTO current_patient,curr_start_date,curr_stop_date
FROM bed_patient_assignment bpam
WHERE bpam.location = locationName
      AND patient_id = patientId AND date_started = dateStopped
LIMIT 1;
if current_patient = patientId  AND curr_start_date = dateStopped THEN
RETURN discharge_date(current_patient, locationName, curr_stop_date);
ELSE
RETURN dateStopped;
END IF ;
END;
$$ LANGUAGE plpgsql;