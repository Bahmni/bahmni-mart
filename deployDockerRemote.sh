#!/usr/bin/env bash

sshpass -p $DEPLOY_PASSWORD ssh $DEPLOY_USERNAME@$DEPLOY_HOST -o stricthostkeychecking=no 'bash deployDocker.sh'
if [ "$?" != 0 ]; then
   exit "$?"
fi