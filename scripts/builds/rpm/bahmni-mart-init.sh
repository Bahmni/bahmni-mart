#!/usr/bin/env bash

read -p "Enter Bahmni-mart Database host name/IP (Optional, default is 'localhost'): " ANALYTICS_DB_SERVER_INPUT
read -p "Enter Bahmni-mart Database username (Optional, default is 'analytics'): " ANALYTICS_DB_USERNAME_INPUT
read -p "Enter Bahmni-mart Database name (Optional, default is 'analytics'): " ANALYTICS_DB_NAME_INPUT
read -p "Enter Postgres username (Optional, default is 'postgres'): " ANALYTICS_DB_USER_POSTGRES_INPUT
read -p "Enter Postgres database PORT (Optional, default is '5432'): " POSTGRESQL_PORT_INPUT

if [ -z "$ANALYTICS_DB_SERVER_INPUT" ]; then
    ANALYTICS_DB_SERVER_INPUT="localhost"
fi

if [ -z "$ANALYTICS_DB_USERNAME_INPUT" ]; then
    ANALYTICS_DB_USERNAME_INPUT="analytics"
fi

if [ -z "$ANALYTICS_DB_NAME_INPUT" ]; then
    ANALYTICS_DB_NAME_INPUT="analytics"
fi

if [ -z "$ANALYTICS_DB_USER_POSTGRES_INPUT" ]; then
    ANALYTICS_DB_USER_POSTGRES_INPUT="postgres"
fi

if [ -z "$POSTGRESQL_PORT_INPUT" ]; then
    POSTGRESQL_PORT_INPUT=5432
fi

export ANALYTICS_DB_SERVER=${ANALYTICS_DB_SERVER_INPUT}
export ANALYTICS_DB_USER=${ANALYTICS_DB_USERNAME_INPUT}
export POSTGRES_DB_NAME=${ANALYTICS_DB_NAME_INPUT}
export ANALYTICS_DB_USER_POSTGRES=${ANALYTICS_DB_USER_POSTGRES_INPUT}
export POSTGRESQL_PORT=${POSTGRESQL_PORT_INPUT}
export POSTGRESQL_VERSION="9.2"

sh /opt/bahmni-mart/bin/bahmni-mart-setup.sh

EXIT_CODE=$?
echo

if [ ${EXIT_CODE}==0 ]; then
    echo "Bahmni mart setup is completed with '${ANALYTICS_DB_USER}' username in '${POSTGRES_DB_NAME}' database on '${ANALYTICS_DB_SERVER}'";
else
    echo "Bahmni mart setup is failed"
    exit ${EXIT_CODE}
fi
