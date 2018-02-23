#!/usr/bin/env bash

./gradlew assemble
if [ "$?" != 0 ]; then
   exit "$?"
fi