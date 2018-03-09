#!/usr/bin/env bash
echo "------------STASHING UNTRACKED AND UNSTAGED FILES------------"

isStashed=`git stash -k -u | grep "Saved working directory and index state WIP on"`
./gradlew check

STATUS=$?

echo "------------UNSTASHING UNTRACKED AND UNSTAGED FILES------------"

if [ "$isStashed" ]
 then
    git stash pop
fi

if [ ${STATUS} != 0 ]
then
	exit 1
fi