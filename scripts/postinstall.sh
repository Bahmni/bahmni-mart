#!/bin/bash

if [ ! -d /opt/bahmni-analytics/conf ]; then
    mkdir -p /opt/bahmni-analytics/conf
fi

#create bahmni user and group if doesn't exist
USERID=bahmni
GROUPID=bahmni
/bin/id -g $GROUPID 2>/dev/null
[ $? -eq 1 ]
groupadd bahmni

/bin/id $USERID 2>/dev/null
[ $? -eq 1 ]
useradd -g bahmni bahmni

#create amman_export directory if it does not exist
if [ ! -d /home/bahmni/amman_export ]; then
    mkdir -p /home/bahmni/amman_export
fi

if [ ! -d /opt/bahmni-analytics/log/ ]; then
    mkdir -p /opt/bahmni-analytics/log/
fi

#create links
ln -s /opt/bahmni-analytics/bin/bahmni-analytics /usr/bin/bahmni-analytics
ln -s /opt/bahmni-analytics/log /var/log/bahmni-analytics
ln -s /home/bahmni/amman_export /opt/bahmni-analytics/amman_export


# permissions
chown -R bahmni:bahmni /usr/bin/bahmni-analytics
chown -R bahmni:bahmni /opt/bahmni-analytics
chown -R bahmni:bahmni /var/log/bahmni-analytics
chown -R bahmni:bahmni /home/bahmni/amman_export

# adding cron job for scheduling the job at 11:30PM everyday
crontab -u bahmni -l | { cat; echo "30 23 * * * /usr/bin/bahmni-analytics >/dev/null 2>&1"; } | crontab -u bahmni -
