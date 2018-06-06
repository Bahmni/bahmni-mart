#!/usr/bin/env bash

read -p "Enter key file path: " KEY_FILE_PATH

if [ -z "$KEY_FILE_PATH" ]; then
    echo "Please provide key file location";
    exit 1:
fi

gpg --import ${KEY_FILE_PATH}