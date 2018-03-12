#!/usr/bin/env bash

../gradlew clean check
if [ "$?" != 0 ]; then
   exit "$?"
fi