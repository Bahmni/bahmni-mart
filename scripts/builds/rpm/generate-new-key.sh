#!/usr/bin/env bash

read -p "Enter Real Name: " REAL_NAME
read -p "Enter E-mail Id: " EMAIL_ID
read -p "Enter comments (Optional): " COMMENT

mkdir -p ~/bahmni-mart-keys;

OUTPUT_FILE_NAME=~/bahmni-mart-keys/${REAL_NAME}.asc

echo "%echo Generating a basic OpenPGP key
 Key-Type: default
 Key-Length: 4096
 Subkey-Type: default
 Subkey-Length: 4096
 Name-Real: $REAL_NAME
 `[[ -z $COMMENT ]] && echo '' || echo Name-Comment: $COMMENT`
 Name-Email: $EMAIL_ID
 Expire-Date: 1y
 %commit" | gpg --batch --generate-key && gpg --armour --export ${REAL_NAME} > ${OUTPUT_FILE_NAME}

EXIT_CODE=$?

echo
if [ ${EXIT_CODE} == 0 ]; then
    echo "New key generation is successfully completed. The key file named as $OUTPUT_FILE_NAME";
else
    echo "New key generation is failed"
    exit ${EXIT_CODE}
fi
