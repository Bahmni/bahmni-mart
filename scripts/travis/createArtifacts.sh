#!/usr/bin/env bash

./gradlew createZip

if [ "$?" != 0 ]; then
   exit "$?"
fi