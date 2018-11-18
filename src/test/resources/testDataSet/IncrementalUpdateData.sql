INSERT INTO markers VALUES('Obs Data', 1, 'Encounter', 'encounter');

DROP TABLE IF EXISTS health_education CASCADE; CREATE TABLE health_education( id_health_education INTEGER PRIMARY KEY, patient_id INTEGER , encounter_id INTEGER , obs_datetime TEXT , date_created TEXT , date_modified TIMESTAMP , location_id INTEGER , location_name TEXT , program_id INTEGER , program_name TEXT , he_marital_status TEXT , he_highest_education_level TEXT , he_pregnancy_status TEXT );

INSERT INTO health_education   (location_name, obs_datetime, date_created, date_modified, he_marital_status, patient_id, id_health_education, program_id, program_name, he_highest_education_level, he_pregnancy_status, encounter_id, location_id) VALUES (NULL, '2018-07-04 15:46:32.0', '2018-07-04 15:46:32.0', NULL, 'Married', 1070, 649021, NULL, NULL, 'No formal education', 'False', 58658, 12);
