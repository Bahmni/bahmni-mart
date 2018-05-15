#!/usr/bin/env bash

echo "Launching bahmni-mart..."
java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.profiles.active="prod" --spring.config.location="/opt/bahmni-mart/conf/" >> /opt/bahmni-mart/log/bahmni-mart.log
echo "Done"
