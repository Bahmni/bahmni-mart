#!/bin/bash -e

echo "Creating the bahmni_mart schema"
psql -c "CREATE SCHEMA bahmni_mart_scdf AUTHORIZATION ${ANALYTICS_DB_USER};" ${POSTGRES_DB_NAME}

echo "Creating the URI_REGISTRY table if not exists in bahmni_mart_scdf schema"
psql -c "CREATE TABLE IF NOT EXISTS bahmni_mart_scdf.URI_REGISTRY ( NAME VARCHAR(255) NOT NULL PRIMARY KEY, URI VARCHAR(255) NOT NULL );" ${POSTGRES_DB_NAME}

echo "Creating the TASK_DEFINITIONS table if not exists in bahmni_mart_scdf schema"
psql -c "CREATE TABLE IF NOT EXISTS bahmni_mart_scdf.TASK_DEFINITIONS ( DEFINITION_NAME VARCHAR(255) NOT NULL PRIMARY KEY, DEFINITION TEXT DEFAULT NULL );" ${POSTGRES_DB_NAME}

echo "Adding bahmni-mart app to SCDF server"
psql -c "INSERT INTO bahmni_mart_scdf.uri_registry VALUES ('task.bahmni-mart', 'file:///opt/bahmni-mart/bahmni-mart.jar');" ${POSTGRES_DB_NAME}

echo "Adding task definition for bahmni-mart app to SCDF server"
psql -c "INSERT INTO bahmni_mart_scdf.task_definitions VALUES ('create-bahmni-mart', 'bahmni-mart --spring.profiles.active=\"prod\" --spring.config.location=\"/opt/bahmni-mart/conf/\"');" ${POSTGRES_DB_NAME}
