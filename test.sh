#!/usr/bin/env bash

./gradlew test
if [ "$?" != 0 ]; then
   exit "$?"
fi