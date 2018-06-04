#!/usr/bin/env bash

#!/usr/bin/env bash

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. ${PATH_OF_CURRENT_SCRIPT}/bahmni-mart-db-details.sh

read -p "Enter PEM file location (Optional, default is '~/.ssh/id_rsa.pub'): " PRIVATE_KEY_FILE_LOCATION
read -p "Enter encrypted DB file location: " BACKUP_FILE_LOCATION

if [ -z "$BACKUP_FILE_LOCATION" ]; then
    echo "Please provide encrypted DB file location";
    exit 1:
fi

if [ -z "$PRIVATE_KEY_FILE_LOCATION" ]; then
    PRIVATE_KEY_FILE_LOCATION=~/.ssh/id_rsa.pub
fi

echo "Restoring  ${ANALYTICS_DB_NAME} with ${BACKUP_FILE_LOCATION}"

openssl smime -decrypt -in ${BACKUP_FILE_LOCATION} -binary -inform DER -inkey ${PRIVATE_KEY_FILE_LOCATION} | bzcat | psql -h ${ANALYTICS_DB_HOST} -p ${POSTGRESQL_PORT} -U ${ANALYTICS_DB_USERNAME} ${ANALYTICS_DB_NAME} 1> /dev/null

EXIT_CODE=$?
echo

if [ ${EXIT_CODE}==0 ]; then
    echo "${ANALYTICS_DB_NAME} restore is successfully completed";
else
    echo "${ANALYTICS_DB_NAME} restore is failed"
    exit ${EXIT_CODE}
fi
