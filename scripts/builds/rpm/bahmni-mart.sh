#!/usr/bin/env bash

display_help(){
    printf "Usage:
    bahmni-mart [OPTIONS]
    By default bahmni-mart application is launched
    Options :
        backup -e: To take encrypted db backup of bahmni-mart DB
            Currently there are two ways to take backup using bahmni-mart
                i. csv - Take backup in Comma Separated Value(CSV) format
                ii. sql - Take backup as sql dump file
            By default sql dump file will be selected
        backup: To take unencrypted backup of bahmni-mart DB. The backup always will be in 'sql' format
        restore FILE-NAME: To restore bahmni-mart db using unencrypted sql dump file
        restore FILE-NAME -e: To restore bahmni-mart db using encrypted sql dump file
        metabase-backup: To take metabase db backup. (only if metabase is installed)
        metabase-restore [FILE NAME]: To restore metabase db using sql dump file. (only if metabase is installed)
        import-key: To import public key of others
        create-key: Create a new public/private key pair
    RUN 'bahmni-mart --help' for more information on a command \n"
}

launch_bahmni_mart(){
    echo "Launching bahmni-mart..."
    nohup java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.config.location="/opt/bahmni-mart/properties/" >> /var/log/bahmni-mart/bahmni-mart.log 2>&1 &
    echo "Done"
}

bahmni_mart_backup(){
    if [[ -z $1 || "$1" == "-e" ]]; then
        sh /opt/bahmni-mart/bin/bahmni-mart-backup.sh $1
    else
        display_help;
    fi
}

metabase_backup(){
    sh /opt/bahmni-mart/bin/metabase-backup.sh
}

bahmni_mart_restore(){
    if [[ -z $1 ]]; then
        echo "Please provide backup DB file location. For more information run bahmni-mart --help";
        exit 1
    fi

    if [[ -z $2 || "$2" == "-e" ]]; then
        sh /opt/bahmni-mart/bin/bahmni-mart-restore.sh $1 $2
    else
        display_help;
    fi
}

import_key(){
    sh /opt/bahmni-mart/bin/import-key.sh
}

create_new_key(){
    sh /opt/bahmni-mart/bin/generate-new-key.sh
}

metabase_restore(){
    sh /opt/bahmni-mart/bin/metabase-restore.sh $1
}

if [[ -z $1 ]]; then
    launch_bahmni_mart;
elif [[ "$1" == "backup" ]]; then
    bahmni_mart_backup $2;
elif [[ "$1" == "import-key" ]]; then
    import_key;
elif [[ "$1" == "create-key" ]]; then
    create_new_key;
elif [[ "$1" == "restore" ]]; then
    bahmni_mart_restore $2 $3;
elif [[ "$1" == "metabase-backup" ]]; then
    metabase_backup;
elif [[ "$1" == "metabase-restore" ]]; then
    metabase_restore $2;
else
    display_help
fi