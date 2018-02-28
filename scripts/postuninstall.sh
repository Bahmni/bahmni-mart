#!/bin/bash

rm /usr/bin/bahmni-mart
rm -rf /var/log/bahmni-mart

rm -rf /opt/bahmni-mart
rm -f /var/www/bahmni_config/mart_export

# removing the cron job
crontab -u bahmni -l | grep -v '/usr/bin/bahmni-mart >/dev/null 2>&1'  | crontab -u bahmni -
