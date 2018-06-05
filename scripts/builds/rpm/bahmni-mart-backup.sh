#!/usr/bin/env bash

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${PATH_OF_CURRENT_SCRIPT}/bahmni-mart-db-details.sh

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
