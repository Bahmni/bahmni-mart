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

import_key(){
    sh /opt/bahmni-mart/bin/import-key.sh
}

create_new_key(){
    sh /opt/bahmni-mart/bin/generate-new-key.sh
}

display_help(){
    printf "Usage:
    bahmni-mart [OPTIONS]
    By default it will launch bahmni-mart application
    Options :
        backup: To take encrypted db backup
            Currently there is 2 way to take backup using bahmni-mart
                i. csv - Take backup in Comma Separated Value(CSV) format
                ii. sql - Take backup as sql dump file
            If you don't provide any format, by default it will use 'sql' format
        restore: To restore db using encrypted sql dump file
        import-key: To import public key of others
        create-key: Create a new public/private key pair
    RUN 'bahmni-mart --help' for more information on a command \n"
}

if [[ -z $1 ]]; then
    launch_bahmni_mart;
elif [[ "$1" == "backup" ]]; then
    bahmni_mart_backup;
elif [[ "$1" == "import-key" ]]; then
    import_key;
elif [[ "$1" == "create-key" ]]; then
    create_new_key;
elif [[ "$1" == "restore" ]]; then
    bahmni_mart_restore;
else
    display_help
fi