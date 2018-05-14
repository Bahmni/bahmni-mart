#!/usr/bin/env bash

. /opt/bahmni-mart/conf/config-variables.conf

echo "Running bahmni-mart..."
java -jar /opt/bahmni-mart/lib/bahmni-mart.jar >> /opt/bahmni-mart/log/bahmni-mart.log
echo "Done"