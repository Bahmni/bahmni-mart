#!/usr/bin/env bash

read -r -p "Enter OpenMRS host name/IP: " MYSQL_HOST_NAME
read -r -p "Enter OpenMRS database name: " OPENMRS_DB_NAME
read -r -p "Enter OpenMRS database username: " MYSQL_USER_NAME
read -r -s -p "Enter OpenMRS database password for the above mentioned user: " PASSWORD
export MYSQL_PWD=${PASSWORD}
>&2 printf "\n--------------------------------------------------------\n"

CONNECTION_STRING="mysql -h ${MYSQL_HOST_NAME} -u${MYSQL_USER_NAME} ${OPENMRS_DB_NAME} -Bse"

CONCEPT_IDS=`${CONNECTION_STRING} "select concept_id from concept_view where concept_full_name = 'All Observation Templates';"`

create_array(){
    CONCEPT_IDS_AS_ARRAY=( $( for CONCEPT_ID in ${CONCEPT_IDS} ; do echo ${CONCEPT_ID} ; done ) )
}

ALL_CONCEPT_IDS_AS_CSV=""

join_by(){
    create_array
    local temp=$(printf ",%s" "${CONCEPT_IDS_AS_ARRAY[@]}")
    if [ ! -z "${ALL_CONCEPT_IDS_AS_CSV}" ]
     then
        ALL_CONCEPT_IDS_AS_CSV+=","
    fi
    AS_CSV=${temp:1}
    ALL_CONCEPT_IDS_AS_CSV+=${AS_CSV}
}

add_concept_ids(){
    join_by
    CONCEPT_IDS=`${CONNECTION_STRING} "select concept_id from concept_set where concept_set in (${AS_CSV})"`

    if [ ! -z "${CONCEPT_IDS}" ]
     then
        add_concept_ids
    fi
}

add_concept_ids

${CONNECTION_STRING} "select concept_full_name from concept_view where retired = 0 and concept_datatype_name = 'Text' AND concept_id in (${ALL_CONCEPT_IDS_AS_CSV})" | while read -r line; do echo "\"$line\""; done | sed '$!s/$/,/'

