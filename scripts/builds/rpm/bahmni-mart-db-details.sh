#!/usr/bin/env bash

read -p "Enter Bahmni-mart Database host name/IP (Optional, default is 'localhost'): " ANALYTICS_DB_HOST
read -p "Enter Bahmni-mart Database username (Optional, default is 'analytics'): " ANALYTICS_DB_USERNAME
read -p "Enter Bahmni-mart Database name (Optional, default is 'analytics'): " ANALYTICS_DB_NAME
read -p "Enter Postgres database PORT (Optional, default is '5432'): " POSTGRESQL_PORT

if [ -z "$ANALYTICS_DB_HOST" ]; then
    ANALYTICS_DB_HOST="localhost"
fi

if [ -z "$ANALYTICS_DB_USERNAME" ]; then
    ANALYTICS_DB_USERNAME="analytics"
fi

if [ -z "$ANALYTICS_DB_NAME" ]; then
    ANALYTICS_DB_NAME="analytics"
fi

if [ -z "$POSTGRESQL_PORT" ]; then
    POSTGRESQL_PORT=5432
fi