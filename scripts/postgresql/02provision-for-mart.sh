#!/bin/bash -e

echo "Creating the bahmni_mart schema"
psql -c "CREATE SCHEMA bahmni_mart_scdf AUTHORIZATION ${ANALYTICS_DB_USER};" ${POSTGRES_DB_NAME}