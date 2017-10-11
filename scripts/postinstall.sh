#!/bin/bash

if [ ! -d /opt/bahmni-batch/conf ]; then
    mkdir -p /opt/bahmni-batch/conf
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

if [ ! -d /opt/bahmni-batch/log/ ]; then
    mkdir -p /opt/bahmni-batch/log/
fi

#create links
ln -s /opt/bahmni-batch/bin/bahmni-batch /usr/bin/bahmni-batch
ln -s /opt/bahmni-batch/log /var/log/bahmni-batch
ln -s /home/bahmni/amman_export /opt/bahmni-batch/amman_export


# permissions
chown -R bahmni:bahmni /usr/bin/bahmni-batch
chown -R bahmni:bahmni /opt/bahmni-batch
chown -R bahmni:bahmni /var/log/bahmni-batch
chown -R bahmni:bahmni /home/bahmni/amman_export

# adding cron job for scheduling the job at 11:30PM everyday
crontab -u bahmni -l | { cat; echo "30 23 * * * /usr/bin/bahmni-batch >/dev/null 2>&1"; } | crontab -u bahmni -
