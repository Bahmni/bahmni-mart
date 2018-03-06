#!/usr/bin/env bash

sshpass -p $DEPLOY_PASSWORD ssh $DEPLOY_USERNAME@$DEPLOY_HOST -o stricthostkeychecking=no 'wget https://raw.githubusercontent.com/bahmni-msf/bahmni-mart/master/deployDocker.sh -O deployDocker.sh'
sshpass -p $DEPLOY_PASSWORD ssh $DEPLOY_USERNAME@$DEPLOY_HOST -o stricthostkeychecking=no 'bash deployDocker.sh'
if [ "$?" != 0 ]; then
   exit "$?"
fi