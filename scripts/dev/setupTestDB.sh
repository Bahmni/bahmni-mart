#!/usr/bin/env bash
PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MYSQL_HOST="192.168.33.10"
MYSQL_HOST_PORT=3306
MYSQL_ROOT_USERNAME="root"
MYSQL_DATABASE_NAME="openmrs"

PSQL_HOST="192.168.33.10"
PSQL_HOST_PORT=5432
PSQL_ROOT_USERNAME="analytics"
PSQL_DATABASE_NAME="analytics"

printf "\nSetting up test_openmrs database\n"
mysql -h ${MYSQL_HOST} -P ${MYSQL_HOST_PORT} -u ${MYSQL_ROOT_USERNAME} -p ${MYSQL_DATABASE_NAME} < ${PATH_OF_CURRENT_SCRIPT}/testMysql.sql

printf "\nSetting up test_analytics database\n"
psql -h ${PSQL_HOST} -p ${PSQL_HOST_PORT} -U ${PSQL_ROOT_USERNAME} -W ${PSQL_DATABASE_NAME} -f ${PATH_OF_CURRENT_SCRIPT}/psqlTestSetup.sql

printf "\nCreating bahmni_mart_scdf schema in test_analytics database\n"
psql -h ${PSQL_HOST} -p ${PSQL_HOST_PORT} -U ${PSQL_ROOT_USERNAME} -W test_analytics -c "CREATE SCHEMA bahmni_mart_scdf;"