#!/usr/bin/env bash

launch_bahmni_mart(){
    echo "Launching bahmni-mart..."
    nohup java -jar /opt/bahmni-mart/lib/bahmni-mart.jar --spring.config.location="/opt/bahmni-mart/properties/" >> /var/log/bahmni-mart/bahmni-mart.log 2>&1 &
    echo "Done"
}

launch_bahmni_mart