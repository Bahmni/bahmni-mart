#!/bin/bash

printenv > /etc/cron.d/crontab
echo "$CRON_TIME \
java -jar /bahmni-mart/app.jar --spring.config.location='/bahmni-mart/application.properties' \
> /proc/1/fd/1 2>/proc/1/fd/2" >> /etc/cron.d/crontab

echo "Setting Cron to Run Jar"
crontab /etc/cron.d/crontab
echo "Running Cron Job"
crond -n