#!/usr/bin/env bash
wget -O bahmni-mart.tar.gz ${BAHMNI_MART_ZIP_URL}
mkdir -p /opt/bahmni-mart && tar -xzf bahmni-mart.tar.gz -C /opt/bahmni-mart
rm -rf bahmni-mart.tar.gz
