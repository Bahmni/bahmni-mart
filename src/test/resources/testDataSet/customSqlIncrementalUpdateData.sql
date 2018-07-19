CREATE TABLE if NOT EXISTS patient_program_data_default (
  patient_program_id INTEGER,
  patient_id         INTEGER
);

INSERT INTO patient_program_data_default VALUES (1, 124);
INSERT INTO patient_program_data_default VALUES (2, 125);
INSERT INTO markers VALUES('Patient program data', 1, 'programenrollment', 'patient_program');