INSERT INTO markers VALUES('Orders Data', 1, 'Encounter', 'encounter');

DROP TABLE IF EXISTS radiology_orders;
CREATE TABLE radiology_orders
(
  patient_id          INTEGER,
  date_created        TIMESTAMP,
  encounter_id        INTEGER,
  encounter_type_id   INTEGER,
  encounter_type_name TEXT,
  visit_type          TEXT,
  visit_type_id       INTEGER,
  type_of_test        TEXT,
  panel_name          TEXT,
  test_name           TEXT
);

INSERT INTO radiology_orders (patient_id, date_created, encounter_id, encounter_type_id, encounter_type_name, visit_type, visit_type_id, type_of_test, panel_name, test_name) VALUES (133, '2018-07-18 05:47:21.000000', 14, 1, 'Consultation',  'Clinic', 4, 'All_Tests_and_Panels', 'Anaemia Panel', 'MCH');
