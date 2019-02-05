SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS test_openmrs.concept_class CASCADE;
CREATE TABLE concept_class
(
  concept_class_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name             VARCHAR(255) DEFAULT '' NOT NULL,
  description      VARCHAR(255)            NULL,
  creator          INT DEFAULT '0'         NOT NULL,
  date_created     DATETIME                NOT NULL,
  retired          TINYINT(1) DEFAULT '0'  NOT NULL,
  retired_by       INT                     NULL,
  date_retired     DATETIME                NULL,
  retire_reason    VARCHAR(255)            NULL,
  uuid             CHAR(38)                NOT NULL,
  date_changed     DATETIME                NULL,
  changed_by       INT                     NULL,
  CONSTRAINT concept_class_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS concept_datatype CASCADE;
CREATE TABLE concept_datatype
(
  concept_datatype_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name                VARCHAR(255) DEFAULT '' NOT NULL,
  hl7_abbreviation    VARCHAR(3)              NULL,
  description         VARCHAR(255)            NULL,
  creator             INT DEFAULT '0'         NOT NULL,
  date_created        DATETIME                NOT NULL,
  retired             BOOLEAN DEFAULT FALSE   NOT NULL,
  retired_by          INT                     NULL,
  date_retired        DATETIME                NULL,
  retire_reason       VARCHAR(255)            NULL,
  uuid                CHAR(38)                NOT NULL,
  CONSTRAINT concept_datatype_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS concept CASCADE;
CREATE TABLE concept
(
  concept_id    INT AUTO_INCREMENT PRIMARY KEY,
  retired       BOOLEAN DEFAULT FALSE  NOT NULL,
  short_name    VARCHAR(255)           NULL,
  description   TEXT                   NULL,
  form_text     TEXT                   NULL,
  datatype_id   INT DEFAULT '0'        NOT NULL,
  class_id      INT DEFAULT '0'        NOT NULL,
  is_set        TINYINT(1) DEFAULT '0' NOT NULL,
  creator       INT DEFAULT '0'        NOT NULL,
  date_created  DATETIME               NOT NULL,
  version       VARCHAR(50)            NULL,
  changed_by    INT                    NULL,
  date_changed  DATETIME               NULL,
  retired_by    INT                    NULL,
  date_retired  DATETIME               NULL,
  retire_reason VARCHAR(255)           NULL,
  uuid          CHAR(38)               NOT NULL,
  CONSTRAINT concept_uuid_index
  UNIQUE (uuid),
  CONSTRAINT concept_datatypes
  FOREIGN KEY (datatype_id) REFERENCES concept_datatype (concept_datatype_id),
  CONSTRAINT concept_classes
  FOREIGN KEY (class_id) REFERENCES concept_class (concept_class_id)
);

DROP TABLE IF EXISTS concept_name CASCADE;
CREATE TABLE concept_name
(
  concept_name_id   INT AUTO_INCREMENT
    PRIMARY KEY,
  concept_id        INT                     NULL,
  name              VARCHAR(255) DEFAULT '' NOT NULL,
  locale            VARCHAR(50) DEFAULT ''  NOT NULL,
  locale_preferred  TINYINT(1) DEFAULT '0'  NULL,
  creator           INT DEFAULT '0'         NOT NULL,
  date_created      DATETIME                NOT NULL,
  concept_name_type VARCHAR(50)             NULL,
  voided            TINYINT(1) DEFAULT '0'  NOT NULL,
  voided_by         INT                     NULL,
  date_voided       DATETIME                NULL,
  void_reason       VARCHAR(255)            NULL,
  uuid              CHAR(38)                NOT NULL,
  date_changed      DATETIME                NULL,
  changed_by        INT                     NULL,
  CONSTRAINT concept_name_uuid_index
  UNIQUE (uuid),
  CONSTRAINT name_for_concept
  FOREIGN KEY (concept_id) REFERENCES concept (concept_id)
);

DROP TABLE IF EXISTS concept_set CASCADE;
CREATE TABLE concept_set
(
  concept_set_id INT AUTO_INCREMENT
    PRIMARY KEY,
  concept_id     INT DEFAULT '0' NOT NULL,
  concept_set    INT DEFAULT '0' NOT NULL,
  sort_weight    DOUBLE          NULL,
  creator        INT DEFAULT '0' NOT NULL,
  date_created   DATETIME        NOT NULL,
  uuid           CHAR(38)        NOT NULL,
  CONSTRAINT concept_set_uuid_index
  UNIQUE (uuid),
  CONSTRAINT has_a
  FOREIGN KEY (concept_set) REFERENCES concept (concept_id)
);

DROP TABLE IF EXISTS concept_map_type CASCADE;
CREATE TABLE concept_map_type
(
  concept_map_type_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name                VARCHAR(255)           NOT NULL,
  description         VARCHAR(255)           NULL,
  creator             INT                    NOT NULL,
  date_created        DATETIME               NOT NULL,
  changed_by          INT                    NULL,
  date_changed        DATETIME               NULL,
  is_hidden           TINYINT(1) DEFAULT '0' NOT NULL,
  retired             BOOLEAN DEFAULT FALSE  NOT NULL,
  retired_by          INT                    NULL,
  date_retired        DATETIME               NULL,
  retire_reason       VARCHAR(255)           NULL,
  uuid                CHAR(38)               NOT NULL,
  CONSTRAINT name
  UNIQUE (name),
  CONSTRAINT uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS concept_reference_source CASCADE;
CREATE TABLE concept_reference_source
(
  concept_source_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name              VARCHAR(50) DEFAULT '' NOT NULL,
  description       TEXT                   NOT NULL,
  hl7_code          VARCHAR(50) DEFAULT '' NULL,
  creator           INT DEFAULT '0'        NOT NULL,
  date_created      DATETIME               NOT NULL,
  retired           BOOLEAN DEFAULT FALSE  NOT NULL,
  retired_by        INT                    NULL,
  date_retired      DATETIME               NULL,
  retire_reason     VARCHAR(255)           NULL,
  uuid              CHAR(38)               NOT NULL,
  unique_id         VARCHAR(250)           NULL,
  date_changed      DATETIME               NULL,
  changed_by        INT                    NULL,
  CONSTRAINT concept_source_unique_hl7_codes
  UNIQUE (hl7_code),
  CONSTRAINT concept_reference_source_uuid_id
  UNIQUE (uuid),
  CONSTRAINT concept_reference_source_unique_id_unique
  UNIQUE (unique_id)
);

DROP TABLE IF EXISTS concept_reference_term CASCADE;
CREATE TABLE concept_reference_term
(
  concept_reference_term_id INT AUTO_INCREMENT
    PRIMARY KEY,
  concept_source_id         INT                   NOT NULL,
  name                      VARCHAR(255)          NULL,
  code                      VARCHAR(255)          NOT NULL,
  version                   VARCHAR(255)          NULL,
  description               VARCHAR(255)          NULL,
  creator                   INT                   NOT NULL,
  date_created              DATETIME              NOT NULL,
  date_changed              DATETIME              NULL,
  changed_by                INT                   NULL,
  retired                   BOOLEAN DEFAULT FALSE NOT NULL,
  retired_by                INT                   NULL,
  date_retired              DATETIME              NULL,
  retire_reason             VARCHAR(255)          NULL,
  uuid                      CHAR(38)              NOT NULL,
  CONSTRAINT concept_reference_term_uuid
  UNIQUE (uuid),
  CONSTRAINT mapped_concept_source
  FOREIGN KEY (concept_source_id) REFERENCES concept_reference_source (concept_source_id)

);

DROP TABLE IF EXISTS concept_reference_map CASCADE;
CREATE TABLE concept_reference_map
(
  concept_map_id            INT AUTO_INCREMENT
    PRIMARY KEY,
  concept_reference_term_id INT             NOT NULL,
  concept_map_type_id       INT DEFAULT '1' NOT NULL,
  creator                   INT DEFAULT '0' NOT NULL,
  date_created              DATETIME        NOT NULL,
  concept_id                INT DEFAULT '0' NOT NULL,
  changed_by                INT             NULL,
  date_changed              DATETIME        NULL,
  uuid                      CHAR(38)        NOT NULL,
  CONSTRAINT concept_reference_map_uuid_id
  UNIQUE (uuid),
  CONSTRAINT mapped_concept_reference_term
  FOREIGN KEY (concept_reference_term_id) REFERENCES concept_reference_term (concept_reference_term_id),
  CONSTRAINT mapped_concept_map_type
  FOREIGN KEY (concept_map_type_id) REFERENCES concept_map_type (concept_map_type_id),
  CONSTRAINT map_for_concept
  FOREIGN KEY (concept_id) REFERENCES concept (concept_id)
);

DROP TABLE IF EXISTS visit_attribute_type CASCADE;
CREATE TABLE visit_attribute_type
(
  visit_attribute_type_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name                    VARCHAR(255)           NOT NULL,
  description             VARCHAR(1024)          NULL,
  datatype                VARCHAR(255)           NULL,
  datatype_config         TEXT                   NULL,
  preferred_handler       VARCHAR(255)           NULL,
  handler_config          TEXT                   NULL,
  min_occurs              INT                    NOT NULL,
  max_occurs              INT                    NULL,
  creator                 INT                    NOT NULL,
  date_created            DATETIME               NOT NULL,
  changed_by              INT                    NULL,
  date_changed            DATETIME               NULL,
  retired                 TINYINT(1) DEFAULT '0' NOT NULL,
  retired_by              INT                    NULL,
  date_retired            DATETIME               NULL,
  retire_reason           VARCHAR(255)           NULL,
  uuid                    CHAR(38)               NOT NULL,
  CONSTRAINT visit_attribute_type_uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS program_attribute_type CASCADE;
CREATE TABLE program_attribute_type
(
  program_attribute_type_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name                      VARCHAR(255)            NOT NULL,
  description               VARCHAR(1024)           NULL,
  datatype                  VARCHAR(255)            NULL,
  datatype_config           TEXT                    NULL,
  preferred_handler         VARCHAR(255)            NULL,
  handler_config            TEXT                    NULL,
  min_occurs                INT                     NOT NULL,
  max_occurs                INT                     NULL,
  creator                   INT                     NOT NULL,
  date_created              DATETIME                NOT NULL,
  changed_by                INT                     NULL,
  date_changed              DATETIME                NULL,
  retired                   SMALLINT(6) DEFAULT '0' NOT NULL,
  retired_by                INT                     NULL,
  date_retired              DATETIME                NULL,
  retire_reason             VARCHAR(255)            NULL,
  uuid                      CHAR(38)                NOT NULL,
  CONSTRAINT program_attribute_type_name
  UNIQUE (name),
  CONSTRAINT program_attribute_type_uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS person CASCADE;
CREATE TABLE person
(
  person_id           INT AUTO_INCREMENT
    PRIMARY KEY,
  gender              VARCHAR(50) DEFAULT '' NULL,
  birthdate           DATE                   NULL,
  birthdate_estimated TINYINT(1) DEFAULT '0' NOT NULL,
  dead                TINYINT(1) DEFAULT '0' NOT NULL,
  death_date          DATETIME               NULL,
  cause_of_death      INT                    NULL,
  creator             INT                    NULL,
  date_created        DATETIME               NOT NULL,
  changed_by          INT                    NULL,
  date_changed        DATETIME               NULL,
  voided              TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by           INT                    NULL,
  date_voided         DATETIME               NULL,
  void_reason         VARCHAR(255)           NULL,
  uuid                CHAR(38)               NOT NULL,
  deathdate_estimated TINYINT(1) DEFAULT '0' NOT NULL,
  birthtime           TIME                   NULL,
  CONSTRAINT person_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS program CASCADE;
CREATE TABLE program
(
  program_id          INT AUTO_INCREMENT
    PRIMARY KEY,
  concept_id          INT DEFAULT '0'        NOT NULL,
  outcomes_concept_id INT                    NULL,
  creator             INT DEFAULT '0'        NOT NULL,
  date_created        DATETIME               NOT NULL,
  changed_by          INT                    NULL,
  date_changed        DATETIME               NULL,
  retired             TINYINT(1) DEFAULT '0' NOT NULL,
  name                VARCHAR(50)            NOT NULL,
  description         TEXT                   NULL,
  uuid                CHAR(38)               NOT NULL,
  CONSTRAINT program_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS patient_state CASCADE;
CREATE TABLE patient_state
(
  patient_state_id   INT AUTO_INCREMENT
    PRIMARY KEY,
  patient_program_id INT DEFAULT '0'        NOT NULL,
  state              INT DEFAULT '0'        NOT NULL,
  start_date         DATE                   NULL,
  end_date           DATE                   NULL,
  creator            INT DEFAULT '0'        NOT NULL,
  date_created       DATETIME               NOT NULL,
  changed_by         INT                    NULL,
  date_changed       DATETIME               NULL,
  voided             TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by          INT                    NULL,
  date_voided        DATETIME               NULL,
  void_reason        VARCHAR(255)           NULL,
  uuid               CHAR(38)               NOT NULL,
  CONSTRAINT patient_state_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS program_workflow_state CASCADE;
CREATE TABLE program_workflow_state
(
  program_workflow_state_id INT AUTO_INCREMENT
    PRIMARY KEY,
  program_workflow_id       INT DEFAULT '0'        NOT NULL,
  concept_id                INT DEFAULT '0'        NOT NULL,
  initial                   TINYINT(1) DEFAULT '0' NOT NULL,
  terminal                  TINYINT(1) DEFAULT '0' NOT NULL,
  creator                   INT DEFAULT '0'        NOT NULL,
  date_created              DATETIME               NOT NULL,
  retired                   TINYINT(1) DEFAULT '0' NOT NULL,
  changed_by                INT                    NULL,
  date_changed              DATETIME               NULL,
  uuid                      CHAR(38)               NOT NULL,
  CONSTRAINT program_workflow_state_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS person_attribute_type CASCADE;
CREATE TABLE person_attribute_type
(
  person_attribute_type_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name                     VARCHAR(50) DEFAULT '' NOT NULL,
  description              TEXT                   NULL,
  format                   VARCHAR(50)            NULL,
  foreign_key              INT                    NULL,
  searchable               TINYINT(1) DEFAULT '0' NOT NULL,
  creator                  INT DEFAULT '0'        NOT NULL,
  date_created             DATETIME               NOT NULL,
  changed_by               INT                    NULL,
  date_changed             DATETIME               NULL,
  retired                  TINYINT(1) DEFAULT '0' NOT NULL,
  retired_by               INT                    NULL,
  date_retired             DATETIME               NULL,
  retire_reason            VARCHAR(255)           NULL,
  edit_privilege           VARCHAR(255)           NULL,
  sort_weight              DOUBLE                 NULL,
  uuid                     CHAR(38)               NOT NULL,
  CONSTRAINT person_attribute_type_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS visit CASCADE;
CREATE TABLE visit
(
  visit_id              INT AUTO_INCREMENT
    PRIMARY KEY,
  patient_id            INT                    NOT NULL,
  visit_type_id         INT                    NOT NULL,
  date_started          DATETIME               NOT NULL,
  date_stopped          DATETIME               NULL,
  indication_concept_id INT                    NULL,
  location_id           INT                    NULL,
  creator               INT                    NOT NULL,
  date_created          DATETIME               NOT NULL,
  changed_by            INT                    NULL,
  date_changed          DATETIME               NULL,
  voided                TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by             INT                    NULL,
  date_voided           DATETIME               NULL,
  void_reason           VARCHAR(255)           NULL,
  uuid                  CHAR(38)               NOT NULL,
  CONSTRAINT uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS patient CASCADE;
CREATE TABLE patient
(
  patient_id     INT                           NOT NULL
    PRIMARY KEY,
  creator        INT DEFAULT '0'               NOT NULL,
  date_created   DATETIME                      NOT NULL,
  changed_by     INT                           NULL,
  date_changed   DATETIME                      NULL,
  voided         TINYINT(1) DEFAULT '0'        NOT NULL,
  voided_by      INT                           NULL,
  date_voided    DATETIME                      NULL,
  void_reason    VARCHAR(255)                  NULL,
  allergy_status VARCHAR(50) DEFAULT 'Unknown' NOT NULL
);

DROP TABLE IF EXISTS encounter CASCADE;
CREATE TABLE encounter
(
  encounter_id       INT AUTO_INCREMENT
    PRIMARY KEY,
  encounter_type     INT                    NOT NULL,
  patient_id         INT DEFAULT '0'        NOT NULL,
  location_id        INT                    NULL,
  form_id            INT                    NULL,
  encounter_datetime DATETIME               NOT NULL,
  creator            INT DEFAULT '0'        NOT NULL,
  date_created       DATETIME               NOT NULL,
  voided             TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by          INT                    NULL,
  date_voided        DATETIME               NULL,
  void_reason        VARCHAR(255)           NULL,
  changed_by         INT                    NULL,
  date_changed       DATETIME               NULL,
  visit_id           INT                    NULL,
  uuid               CHAR(38)               NOT NULL,
  CONSTRAINT encounter_uuid_index
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS obs CASCADE;
CREATE TABLE obs
(
  obs_id                  INT AUTO_INCREMENT
    PRIMARY KEY,
  person_id               INT                         NOT NULL,
  concept_id              INT DEFAULT '0'             NOT NULL,
  encounter_id            INT                         NULL,
  order_id                INT                         NULL,
  obs_datetime            DATETIME                    NOT NULL,
  location_id             INT                         NULL,
  obs_group_id            INT                         NULL,
  accession_number        VARCHAR(255)                NULL,
  value_group_id          INT                         NULL,
  value_coded             INT                         NULL,
  value_coded_name_id     INT                         NULL,
  value_drug              INT                         NULL,
  value_datetime          DATETIME                    NULL,
  value_numeric           DOUBLE                      NULL,
  value_modifier          VARCHAR(2)                  NULL,
  value_text              TEXT                        NULL,
  value_complex           VARCHAR(1000)               NULL,
  comments                VARCHAR(255)                NULL,
  creator                 INT DEFAULT '0'             NOT NULL,
  date_created            DATETIME                    NOT NULL,
  voided                  TINYINT(1) DEFAULT '0'      NOT NULL,
  voided_by               INT                         NULL,
  date_voided             DATETIME                    NULL,
  void_reason             VARCHAR(255)                NULL,
  uuid                    CHAR(38)                    NOT NULL,
  previous_version        INT                         NULL,
  form_namespace_and_path VARCHAR(255)                NULL,
  status                  VARCHAR(16) DEFAULT 'FINAL' NOT NULL,
  interpretation          VARCHAR(32)                 NULL,
  CONSTRAINT obs_concept
  FOREIGN KEY (concept_id) REFERENCES concept (concept_id),
  CONSTRAINT encounter_observations
  FOREIGN KEY (encounter_id) REFERENCES encounter (encounter_id),
  CONSTRAINT obs_grouping_id
  FOREIGN KEY (obs_group_id) REFERENCES obs (obs_id),
  CONSTRAINT answer_concept
  FOREIGN KEY (value_coded) REFERENCES concept (concept_id),
  CONSTRAINT obs_name_of_coded_value
  FOREIGN KEY (value_coded_name_id) REFERENCES concept_name (concept_name_id),
  CONSTRAINT previous_version
  FOREIGN KEY (previous_version) REFERENCES obs (obs_id)
);

DROP TABLE IF EXISTS patient_program CASCADE;
CREATE TABLE patient_program
(
  patient_program_id INT AUTO_INCREMENT
    PRIMARY KEY,
  patient_id         INT DEFAULT '0'        NOT NULL,
  program_id         INT DEFAULT '0'        NOT NULL,
  date_enrolled      DATETIME               NULL,
  date_completed     DATETIME               NULL,
  location_id        INT                    NULL,
  outcome_concept_id INT                    NULL,
  creator            INT DEFAULT '0'        NOT NULL,
  date_created       DATETIME               NOT NULL,
  changed_by         INT                    NULL,
  date_changed       DATETIME               NULL,
  voided             TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by          INT                    NULL,
  date_voided        DATETIME               NULL,
  void_reason        VARCHAR(255)           NULL,
  uuid               CHAR(38)               NOT NULL,
  CONSTRAINT patient_program_uuid_index
  UNIQUE (uuid)
);

DROP VIEW IF EXISTS concept_view CASCADE;
CREATE VIEW concept_view AS
  SELECT
    concept.concept_id      AS concept_id,
    concept_full_name.name  AS concept_full_name,
    concept_short_name.name AS concept_short_name,
    concept_class.name      AS concept_class_name,
    concept_datatype.name   AS concept_datatype_name,
    concept.retired         AS retired,
    concept.date_created    AS date_created
  FROM ((((concept
    LEFT JOIN concept_name concept_full_name
      ON (((concept_full_name.concept_id = concept.concept_id) AND
           (concept_full_name.concept_name_type = 'FULLY_SPECIFIED') AND (concept_full_name.locale = 'en') AND
           (concept_full_name.voided = 0)))) LEFT JOIN
    concept_name concept_short_name
      ON (((concept_short_name.concept_id = concept.concept_id) AND
           (concept_short_name.concept_name_type = 'SHORT') AND (concept_short_name.locale = 'en') AND
           (concept_short_name.voided = 0)))) LEFT JOIN
    concept_class
      ON ((concept_class.concept_class_id = concept.class_id))) LEFT JOIN
    concept_datatype
      ON ((concept_datatype.concept_datatype_id = concept.datatype_id)));

DROP VIEW IF EXISTS concept_reference_term_map_view CASCADE;
CREATE VIEW concept_reference_term_map_view AS
  SELECT
    concept_reference_map.concept_id AS concept_id,
    concept_map_type.name            AS concept_map_type_name,
    concept_reference_term.code      AS code,
    concept_reference_term.name      AS concept_reference_term_name,
    concept_reference_source.name    AS concept_reference_source_name
  FROM (((concept_reference_term
    JOIN concept_reference_map
      ON ((concept_reference_map.concept_reference_term_id =
           concept_reference_term.concept_reference_term_id))) JOIN
    concept_map_type
      ON ((concept_reference_map.concept_map_type_id =
           concept_map_type.concept_map_type_id))) JOIN
    concept_reference_source
      ON ((concept_reference_source.concept_source_id =
           concept_reference_term.concept_source_id)));

DROP TABLE IF EXISTS order_type CASCADE;
CREATE TABLE order_type
(
  order_type_id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name            VARCHAR(255) DEFAULT '' NOT NULL,
  description     TEXT                    NULL,
  creator         INT DEFAULT '0'         NOT NULL,
  date_created    DATETIME                NOT NULL,
  retired         TINYINT(1) DEFAULT '0'  NOT NULL,
  retired_by      INT                     NULL,
  date_retired    DATETIME                NULL,
  retire_reason   VARCHAR(255)            NULL,
  uuid            CHAR(38)                NOT NULL,
  java_class_name VARCHAR(255)            NOT NULL,
  parent          INT                     NULL,
  changed_by      INT                     NULL,
  date_changed    DATETIME                NULL,
  CONSTRAINT unique_name
  UNIQUE (name),
  CONSTRAINT unique_uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS order_type_class_map CASCADE;
CREATE TABLE order_type_class_map
(
  order_type_id    INT NOT NULL,
  concept_class_id INT NOT NULL,
  PRIMARY KEY (order_type_id, concept_class_id),
  CONSTRAINT concept_class_id
  UNIQUE (concept_class_id),
  CONSTRAINT fk_order_type_order_type_id
  FOREIGN KEY (order_type_id) REFERENCES order_type (order_type_id),
  CONSTRAINT fk_order_type_class_map_concept_class_concept_class_id
  FOREIGN KEY (concept_class_id) REFERENCES concept_class (concept_class_id)
);

DROP TABLE IF EXISTS visit_type CASCADE;
CREATE TABLE visit_type
(
  visit_type_id INT
    PRIMARY KEY,
  name          VARCHAR(255)           NOT NULL,
  description   VARCHAR(1024)          NULL,
  creator       INT                    NOT NULL,
  date_created  DATETIME               NOT NULL,
  changed_by    INT                    NULL,
  date_changed  DATETIME               NULL,
  retired       TINYINT(1) DEFAULT '0' NOT NULL,
  retired_by    INT                    NULL,
  date_retired  DATETIME               NULL,
  retire_reason VARCHAR(255)           NULL,
  uuid          CHAR(38)               NOT NULL,
  CONSTRAINT visit_type_uuid
  UNIQUE (uuid)
);

DROP TABLE IF EXISTS visit CASCADE;
CREATE TABLE visit
(
  visit_id              INT AUTO_INCREMENT
    PRIMARY KEY,
  patient_id            INT                    NOT NULL,
  visit_type_id         INT                    NOT NULL,
  date_started          DATETIME               NOT NULL,
  date_stopped          DATETIME               NULL,
  indication_concept_id INT                    NULL,
  location_id           INT                    NULL,
  creator               INT                    NOT NULL,
  date_created          DATETIME               NOT NULL,
  changed_by            INT                    NULL,
  date_changed          DATETIME               NULL,
  voided                TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by             INT                    NULL,
  date_voided           DATETIME               NULL,
  void_reason           VARCHAR(255)           NULL,
  uuid                  CHAR(38)               NOT NULL,
  CONSTRAINT visit_uuid
  UNIQUE (uuid),
  CONSTRAINT visit_patient_fk
  FOREIGN KEY (patient_id) REFERENCES patient (patient_id),
  CONSTRAINT visit_type_fk
  FOREIGN KEY (visit_type_id) REFERENCES visit_type (visit_type_id)
);

DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders
(
  order_id               INT AUTO_INCREMENT
    PRIMARY KEY,
  order_type_id          INT                           NULL,
  concept_id             INT DEFAULT '0'               NOT NULL,
  orderer                INT                           NOT NULL,
  encounter_id           INT                           NOT NULL,
  instructions           TEXT                          NULL,
  date_activated         DATETIME                      NULL,
  auto_expire_date       DATETIME                      NULL,
  date_stopped           DATETIME                      NULL,
  order_reason           INT                           NULL,
  order_reason_non_coded VARCHAR(255)                  NULL,
  creator                INT DEFAULT '0'               NOT NULL,
  date_created           DATETIME                      NOT NULL,
  voided                 TINYINT(1) DEFAULT '0'        NOT NULL,
  voided_by              INT                           NULL,
  date_voided            DATETIME                      NULL,
  void_reason            VARCHAR(255)                  NULL,
  patient_id             INT                           NOT NULL,
  accession_number       VARCHAR(255)                  NULL,
  uuid                   CHAR(38)                      NOT NULL,
  urgency                VARCHAR(50) DEFAULT 'ROUTINE' NOT NULL,
  order_number           VARCHAR(50)                   NOT NULL,
  previous_order_id      INT                           NULL,
  order_action           VARCHAR(50)                   NOT NULL,
  comment_to_fulfiller   VARCHAR(1024)                 NULL,
  care_setting           INT                           NOT NULL,
  scheduled_date         DATETIME                      NULL,
  order_group_id         INT                           NULL,
  sort_weight            DOUBLE                        NULL,
  CONSTRAINT orders_uuid_index
  UNIQUE (uuid),
  CONSTRAINT type_of_order
  FOREIGN KEY (order_type_id) REFERENCES order_type (order_type_id),
  CONSTRAINT order_for_patient
  FOREIGN KEY (patient_id) REFERENCES patient (patient_id)
    ON UPDATE CASCADE
);

DROP TABLE IF EXISTS encounter_type CASCADE;
CREATE TABLE encounter_type
(
  encounter_type_id INT AUTO_INCREMENT
    PRIMARY KEY,
  name              VARCHAR(50) DEFAULT '' NOT NULL,
  description       TEXT                   NULL,
  creator           INT DEFAULT '0'        NOT NULL,
  date_created      DATETIME               NOT NULL,
  retired           TINYINT(1) DEFAULT '0' NOT NULL,
  retired_by        INT                    NULL,
  date_retired      DATETIME               NULL,
  retire_reason     VARCHAR(255)           NULL,
  uuid              CHAR(38)               NOT NULL,
  edit_privilege    VARCHAR(255)           NULL,
  view_privilege    VARCHAR(255)           NULL,
  changed_by        INT                    NULL,
  date_changed      DATETIME               NULL
);

DROP TABLE IF EXISTS location CASCADE;
CREATE TABLE location
(
  location_id     INT AUTO_INCREMENT
    PRIMARY KEY,
  name            VARCHAR(255) DEFAULT '' NOT NULL,
  description     VARCHAR(255)            NULL,
  address1        VARCHAR(255)            NULL,
  address2        VARCHAR(255)            NULL,
  city_village    VARCHAR(255)            NULL,
  state_province  VARCHAR(255)            NULL,
  postal_code     VARCHAR(50)             NULL,
  country         VARCHAR(50)             NULL,
  latitude        VARCHAR(50)             NULL,
  longitude       VARCHAR(50)             NULL,
  creator         INT DEFAULT '0'         NOT NULL,
  date_created    DATETIME                NOT NULL,
  county_district VARCHAR(255)            NULL,
  address3        VARCHAR(255)            NULL,
  address4        VARCHAR(255)            NULL,
  address5        VARCHAR(255)            NULL,
  address6        VARCHAR(255)            NULL,
  retired         TINYINT(1) DEFAULT '0'  NOT NULL,
  retired_by      INT                     NULL,
  date_retired    DATETIME                NULL,
  retire_reason   VARCHAR(255)            NULL,
  parent_location INT                     NULL,
  uuid            CHAR(38)                NOT NULL,
  changed_by      INT                     NULL,
  date_changed    DATETIME                NULL,
  address7        VARCHAR(255)            NULL,
  address8        VARCHAR(255)            NULL,
  address9        VARCHAR(255)            NULL,
  address10       VARCHAR(255)            NULL,
  address11       VARCHAR(255)            NULL,
  address12       VARCHAR(255)            NULL,
  address13       VARCHAR(255)            NULL,
  address14       VARCHAR(255)            NULL,
  address15       VARCHAR(255)            NULL
);

DROP TABLE IF EXISTS episode_encounter CASCADE;
CREATE TABLE episode_encounter
(
  episode_id   INT NOT NULL,
  encounter_id INT NOT NULL
);

DROP TABLE IF EXISTS episode_patient_program CASCADE;
CREATE TABLE episode_patient_program
(
  episode_id         INT NOT NULL,
  patient_program_id INT NOT NULL
);

DROP TABLE IF EXISTS person_attribute CASCADE;
CREATE TABLE person_attribute
(
  person_attribute_id      INT AUTO_INCREMENT
    PRIMARY KEY,
  person_id                INT DEFAULT '0'        NOT NULL,
  value                    VARCHAR(50) DEFAULT '' NOT NULL,
  person_attribute_type_id INT DEFAULT '0'        NOT NULL,
  creator                  INT DEFAULT '0'        NOT NULL,
  date_created             DATETIME               NOT NULL,
  changed_by               INT                    NULL,
  date_changed             DATETIME               NULL,
  voided                   TINYINT(1) DEFAULT '0' NOT NULL,
  voided_by                INT                    NULL,
  date_voided              DATETIME               NULL,
  void_reason              VARCHAR(255)           NULL,
  uuid                     CHAR(38)               NOT NULL
);

DROP TABLE IF EXISTS event_records CASCADE;
create table event_records
(
	id int auto_increment
		primary key,
	uuid varchar(40) null,
	title varchar(255) null,
	timestamp timestamp default CURRENT_TIMESTAMP not null,
	uri varchar(255) null,
	object varchar(1000) null,
	category varchar(255) null,
	date_created timestamp default CURRENT_TIMESTAMP not null,
	tags varchar(255) null,
	constraint id
		unique (id)
);

DROP TABLE IF EXISTS form CASCADE;
create table form
(
	form_id int auto_increment
		primary key,
	name varchar(255) default '' not null,
	version varchar(50) default '' not null,
	build int null,
	published tinyint(1) default '0' not null,
	xslt text null,
	template text null,
	description text null,
	encounter_type int null,
	creator int default '0' not null,
	date_created datetime not null,
	changed_by int null,
	date_changed datetime null,
	retired tinyint(1) default '0' not null,
	retired_by int null,
	date_retired datetime null,
	retired_reason varchar(255) null,
	uuid char(38) not null,
	constraint form_uuid_index
		unique (uuid)
);


DROP TABLE IF EXISTS form_resource CASCADE;
create table form_resource
(
	form_resource_id int auto_increment
		primary key,
	form_id int not null,
	name varchar(255) not null,
	value_reference text not null,
	datatype varchar(255) null,
	datatype_config text null,
	preferred_handler varchar(255) null,
	handler_config text null,
	uuid char(38) not null,
	date_changed datetime null,
	changed_by int null,
	constraint unique_form_and_name
		unique (form_id, name),
	constraint uuid
		unique (uuid),
	constraint form_resource_changed_by
		foreign key (changed_by) references users (user_id),
	constraint form_resource_form_fk
		foreign key (form_id) references form (form_id)
);

SET FOREIGN_KEY_CHECKS=1;

