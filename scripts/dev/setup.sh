#!/usr/bin/env bash
echo "-----------SETUP IS STARTED-----------"

PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PATH_OF_GIT_HOOK_DIR="${PATH_OF_CURRENT_SCRIPT}/../../.git/hooks/"

ln -s ${PATH_OF_CURRENT_SCRIPT}/pre-push.sh ${PATH_OF_GIT_HOOK_DIR}/pre-push && chmod +x ${PATH_OF_GIT_HOOK_DIR}/pre-push
ln -s ${PATH_OF_CURRENT_SCRIPT}/commit-msg ${PATH_OF_GIT_HOOK_DIR}/commit-msg && chmod +x ${PATH_OF_GIT_HOOK_DIR}/commit-msg

sh ${PATH_OF_CURRENT_SCRIPT}/setupTestDB.sh

if [ $? != 0 ]
then
echo "-----------SETUP IS FAILED-----------"
	exit 1
fi

echo "-----------SETUP IS COMPLETED-----------"