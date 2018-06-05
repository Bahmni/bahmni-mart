#!/usr/bin/env bash

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${PATH_OF_CURRENT_SCRIPT}/bahmni-mart-db-details.sh

read -p "Enter PEM file location (Optional, default is '~/.ssh/id_rsa'): " PUBLIC_KEY_FILE_LOCATION

OUTPUT_FILE_NAME=/data/analytics/analytics_$(date +"%Y%m%d%H%M").dump

if [ -z "$PUBLIC_KEY_FILE_LOCATION" ]; then
    PUBLIC_KEY_FILE_LOCATION=~/.ssh/id_rsa.pub
fi

echo "Creating encrypted backup file ${OUTPUT_FILE_NAME}"

pg_dump -h ${ANALYTICS_DB_HOST} -p ${POSTGRESQL_PORT} -U ${ANALYTICS_DB_USERNAME} ${ANALYTICS_DB_NAME} -c | bzip2 | openssl smime -encrypt -aes256 -binary -outform DER -out ${OUTPUT_FILE_NAME} ${PUBLIC_KEY_FILE_LOCATION}

EXIT_CODE=$?
echo

if [ ${EXIT_CODE} == 0 ]; then
    echo "Backup of ${ANALYTICS_DB_NAME} is successfully completed";
else
    echo "Backup of ${ANALYTICS_DB_NAME} is failed"
    exit ${EXIT_CODE}
fi
