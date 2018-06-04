#!/usr/bin/env bash

launch_bahmni_mart(){
    echo "Launching bahmni-mart..."
    nohup java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.config.location="/opt/bahmni-mart/properties/" >> /var/log/bahmni-mart/bahmni-mart.log 2>&1 &
    echo "Done"
}

bahmni_mart_backup(){
    sh /opt/bahmni-mart/bin/bahmni-mart-backup.sh
}

bahmni_mart_restore(){
    sh /opt/bahmni-mart/bin/bahmni-mart-restore.sh
}

display_help(){
    printf "Usage:
    bahmni-mart [OPTIONS]
    By default it will launch bahmni-mart application
    Options :
        backup: To take encrypted db backup
        restore: To restore encrypted db
    RUN 'bahmni-mart --help' for more information on a command \n"
}

if [[ -z $1 ]]; then
    launch_bahmni_mart;
elif [[ "$1" == "backup" ]]; then
    bahmni_mart_backup;
elif [[ "$1" == "restore" ]]; then
    bahmni_mart_restore;
else
    display_help
fi