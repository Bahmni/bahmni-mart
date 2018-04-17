#!/usr/bin/env bash

./gradlew -Dspring.profiles.active=ci clean check
if [ "$?" != 0 ]; then
   exit "$?"
fi
