INSERT INTO markers VALUES ('Form2 Obs Data', 1, 'Encounter', 'encounter');

DROP TABLE IF EXISTS formbuilderformincremental CASCADE;
CREATE TABLE formbuilderformincremental (
  patient_id                  integer,
  obs_datetime                timestamp,
  date_created                timestamp,
  date_modified               timestamp,
  location_id                 integer,
  location_name               text,
  program_id                  integer,
  program_name                text,
  form_field_path             text    not null,
  encounter_id                integer not null,
  visit_id                    integer not null,
  patient_program_id          integer,
  wwn_systolic_blood_pressure numeric,
  primary key (form_field_path, encounter_id)
);

INSERT INTO formbuilderformincremental (patient_id, obs_datetime, date_created, date_modified, location_id, location_name, program_id, program_name, form_field_path, encounter_id, visit_id, patient_program_id, wwn_systolic_blood_pressure)
VALUES (94, '2019-02-19 09:46:28.000000', '2019-02-19 10:03:52.000000', null, 3, 'Registration Desk', 7,
  'Second-line TB treatment register', 'FormBuilderFormIncremental', 3837, 3, 9, '5');
