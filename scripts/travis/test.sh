#!/usr/bin/env bash

./gradlew -Dspring.profiles.active=ci clean check

exit "$?"

