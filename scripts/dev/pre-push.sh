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

versionLineNumber=`( grep -n "version =" build.gradle | cut -d' ' -f1 | sed 's/^\(..\).*/\1/')`
lineCommit=`(git log  -L $versionLineNumber,$versionLineNumber:build.gradle --max-count=1 --pretty=format:"%H" | head -n 1)`
currentBranch=`git branch | sed -n '/\* /s///p'`
if [ $currentBranch == "master" ]; then
        git cherry -v origin/$currentBranch | cut -d' ' -f2 | tee localCommits.txt
        else
        exit 0;
fi
if [ `grep $lineCommit localCommits.txt`  ]; then
        rm -rf localCommits.txt
        exit 0;
        else
        echo "Please update bahmni-mart build version with every git push to master branch"
        rm -rf localCommits.txt
        exit 1;
fi

if [ ${STATUS} != 0 ]
then
	exit 1
fi
