#!/bin/bash

manage_user_and_group() {
    #create bahmni user and group if doesn't exist
    USERID=bahmni
    GROUPID=bahmni
    /bin/id -g $GROUPID 2>/dev/null
    [ $? -eq 1 ]
    groupadd bahmni

    /bin/id $USERID 2>/dev/null
    [ $? -eq 1 ]
    useradd -g bahmni bahmni
}

create_mart_directories() {
    if [ ! -d /opt/bahmni-mart/log/ ]; then
        mkdir -p /opt/bahmni-mart/log/
    fi
}

link_directories() {
    #create links
    ln -s /opt/bahmni-mart/bin/bahmni-mart.sh /usr/bin/bahmni-mart
    ln -s /opt/bahmni-mart/log /var/log/bahmni-mart
}

manage_permissions() {
    # permissions
    chown -R bahmni:bahmni /usr/bin/bahmni-mart
    chown -R bahmni:bahmni /opt/bahmni-mart
    chown -R bahmni:bahmni /var/log/bahmni-mart
}
setup_cronjob() {
    # adding cron job for scheduling the job at 11:30PM everyday
    crontab -u bahmni -l | { cat; echo "30 23 * * * /usr/bin/bahmni-mart >/dev/null 2>&1"; } | crontab -u bahmni -
}

manage_user_and_group
create_mart_directories
link_directories
manage_permissions
#setup_cronjob