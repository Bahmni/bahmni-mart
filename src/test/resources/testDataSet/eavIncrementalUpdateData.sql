CREATE TABLE IF NOT EXISTS test_person_attributes (
  person_id INTEGER NOT NULL PRIMARY KEY,
  "givenNameLocal" TEXT,
  "familyNameLocal" TEXT,
  "middleNameLocal" TEXT,
  viber TEXT,
  "phoneNumber2" TEXT
);

INSERT INTO test_person_attributes (person_id, "givenNameLocal", "familyNameLocal", "middleNameLocal", viber, "phoneNumber2") VALUES (124, 'Superman', 'Superhero', NULL, NULL, '000000000');
INSERT INTO test_person_attributes (person_id, "givenNameLocal", "familyNameLocal", "middleNameLocal", viber, "phoneNumber2") VALUES (125, 'John', NULL, NULL, NULL, NULL);
INSERT INTO markers (job_name, event_record_id, category, table_name) VALUES ('Test Person Attributes', 1, 'patient', 'person');
