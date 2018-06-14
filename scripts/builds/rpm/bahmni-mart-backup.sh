#!/usr/bin/env bash

ANALYTICS_DB_HOST="localhost"
ANALYTICS_DB_USERNAME="analytics"
ANALYTICS_DB_NAME="analytics"
POSTGRESQL_PORT=5432

read -p "Enter recipient name(s) with comma(,) separation: " RECIPIENT_AS_CSV
read -p "Enter DB backup format csv/sql. (Optional, default is 'sql'): " FORMAT

OUTPUT_DATA_DIRECTORY=/data/analytics
ACTUAL_FILE_NAME=analytics_$(date +"%Y%m%d%H%M")

BASE_OUTPUT_FILE_NAME=${OUTPUT_DATA_DIRECTORY}/${ACTUAL_FILE_NAME}
RECIPIENTS=""

print_info(){
    echo "Creating encrypted backup file $1"
}

export_as_csv(){
    print_info $1
    mkdir -p ${BASE_OUTPUT_FILE_NAME}
    psql -U analytics -Atc "select tablename from pg_tables where schemaname='public'" ${ANALYTICS_DB_NAME} |\
      while read TABLE_NAME; do
        psql -U analytics -c "COPY public.$TABLE_NAME TO STDOUT WITH CSV HEADER" ${ANALYTICS_DB_NAME} > ${BASE_OUTPUT_FILE_NAME}/${TABLE_NAME}.csv
      done

    ZIP_FILE_NAME=${BASE_OUTPUT_FILE_NAME}.zip

    (cd ${BASE_OUTPUT_FILE_NAME}/.. && tar --remove-files -czf ${ZIP_FILE_NAME} ${ACTUAL_FILE_NAME} )
    gpg ${RECIPIENTS} --always-trust --encrypt -o $1 ${ZIP_FILE_NAME} && rm -rf ${ZIP_FILE_NAME}
}

export_as_sql(){
    print_info $1

    pg_dump -h ${ANALYTICS_DB_HOST} -p ${POSTGRESQL_PORT} -U ${ANALYTICS_DB_USERNAME} ${ANALYTICS_DB_NAME} -c | gpg ${RECIPIENTS} --always-trust --encrypt -o $1
}

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

if [ "${FORMAT,,}" = "csv" ]; then
    export_as_csv ${BASE_OUTPUT_FILE_NAME}.gpg
elif [[ "${FORMAT,,}" = "sql" || -z "$FORMAT" ]]; then
    export_as_sql ${BASE_OUTPUT_FILE_NAME}.dump.gpg
else
    echo "Please provide proper format. For more info run 'bahmni-mart --help' "
    exit 1;
fi

EXIT_CODE=$?

if [ ${EXIT_CODE} == 0 ]; then
    echo "Backup of ${ANALYTICS_DB_NAME} is successfully completed";
else
    echo "Backup of ${ANALYTICS_DB_NAME} is failed"
    exit ${EXIT_CODE}
fi
