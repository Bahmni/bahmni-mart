#!/usr/bin/env bash

./gradlew clean assemble
if [ "$?" != 0 ]; then
   exit "$?"
fi
