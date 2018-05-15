#!/usr/bin/env bash

echo "Launching bahmni-mart..."
nohup java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.profiles.active="host" --spring.config.location="/opt/bahmni-mart/conf/" >> /opt/bahmni-mart/log/bahmni-mart.log 2>&1 &
echo "Done"
