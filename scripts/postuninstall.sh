#!/bin/bash

rm /usr/bin/bahmni-analytics
rm -rf /var/log/bahmni-analytics

rm -rf /opt/bahmni-analytics
rm -f /var/www/bahmni_config/analytics_export

# removing the cron job
crontab -u bahmni -l | grep -v '/usr/bin/bahmni-analytics >/dev/null 2>&1'  | crontab -u bahmni -
