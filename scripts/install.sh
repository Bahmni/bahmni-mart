#!/usr/bin/env bash
wget -O bahmni-mart.tar.gz "https://s3.ap-south-1.amazonaws.com/bahmni-mart/travis-builds/bahmni-mart.tar.gz"
mkdir -p /opt/bahmni-mart && tar -xvzf bahmni-mart.tar.gz -C /opt/bahmni-mart
rm -rf bahmni-mart.tar.gz
