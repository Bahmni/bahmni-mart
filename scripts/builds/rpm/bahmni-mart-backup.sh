#!/usr/bin/env bash

ANALYTICS_DB_HOST="localhost"
ANALYTICS_DB_USERNAME="analytics"
ANALYTICS_DB_NAME="analytics"
POSTGRESQL_PORT=5432

read -p "Enter recipient name(s) with comma(,) separation: " RECIPIENT_AS_CSV

OUTPUT_FILE_NAME=/data/analytics/analytics_$(date +"%Y%m%d%H%M").dump.gpg
RECIPIENTS=""

echo

if [ ! -z "$RECIPIENT_AS_CSV" ]; then
    IFS=',' read -ra ADDR <<< "$RECIPIENT_AS_CSV"
    for i in "${ADDR[@]}"; do
        RECIPIENTS="$RECIPIENTS -r $i"
    done
else
    echo "Please provide recipient name(s)"
    exit 1;
fi

echo "Creating encrypted backup file ${OUTPUT_FILE_NAME}"

pg_dump -h ${ANALYTICS_DB_HOST} -p ${POSTGRESQL_PORT} -U ${ANALYTICS_DB_USERNAME} ${ANALYTICS_DB_NAME} -c | gpg ${RECIPIENTS} --always-trust --encrypt -o ${OUTPUT_FILE_NAME}

EXIT_CODE=$?

if [ ${EXIT_CODE} == 0 ]; then
    echo "Backup of ${ANALYTICS_DB_NAME} is successfully completed";
else
    echo "Backup of ${ANALYTICS_DB_NAME} is failed"
    exit ${EXIT_CODE}
fi
