#!/usr/bin/env bash

sudo docker pull anallytics/bahmni-mart
sudo wget https://raw.githubusercontent.com/bahmni-msf/bahmni-mart/master/conf/bahmni-mart.conf -O bahmni-mart.conf
sudo docker stop mart
sudo docker rm $(docker ps -a -f status=exited -q)
sudo docker run --privileged=true --name mart -p 4432:5432 -v $(pwd)/bahmni-mart.conf:/opt/bahmni-mart/conf/bahmni-mart.conf -i bahmni-mart