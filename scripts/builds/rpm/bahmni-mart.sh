#!/usr/bin/env bash


initialize(){
    echo "Starting Bahmni Mart initialization..."
    sh /opt/bahmni-mart/bin/bahmni-mart-init.sh
    echo "Done"
}

launch_bahmni_mart(){
    echo "Launching bahmni-mart..."
    nohup java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.profiles.active="host" --spring.config.location="/var/www/bahmni_config/bahmni-mart/" >> /var/log/bahmni-mart/bahmni-mart.log 2>&1 &
    echo "Done"
}

if [[ ! -z $1 && "${1,,}" == "init" ]]; then
    initialize
else
    launch_bahmni_mart
fi