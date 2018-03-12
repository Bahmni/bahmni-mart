#!/bin/bash -e

echo "Creating the mart role"
psql <<-EOSQL
    CREATE USER "${ANALYTICS_DB_USER}" CREATEDB REPLICATION NOCREATEROLE SUPERUSER;
EOSQL

echo "Creating the analytics DB"
psql <<-EOSQL
    CREATE DATABASE "${POSTGRES_DB_NAME}" WITH OWNER analytics ENCODING='UTF-8';
EOSQL

echo "Revoke access for other users from analytics"
psql <<-EOSQL
    REVOKE ALL PRIVILEGES ON DATABASE "${POSTGRES_DB_NAME}" FROM public;
EOSQL