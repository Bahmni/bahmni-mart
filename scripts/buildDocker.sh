#!/usr/bin/env bash

export COMMIT=${TRAVIS_COMMIT::8}

docker login -u $DOCKER_USER -p $DOCKER_PASS
export JAVAREPO=anallytics/bahmni-mart
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
docker build -f JavaDockerfile -t $REPO:$COMMIT .
docker tag $JAVAREPO:$COMMIT $REPO:$TAG
docker tag $JAVAREPO:$COMMIT $REPO:mart-$TRAVIS_BUILD_NUMBER
docker push $JAVAREPO

export POSTGRESREPO=anallytics/postgres
export TAG=$(if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi)
docker build -f PostgresqlDockerfile -t $REPO:$COMMIT .
docker tag $POSTGRESREPO:$COMMIT $REPO:$TAG
docker tag $POSTGRESREPO:$COMMIT $REPO:mart-$TRAVIS_BUILD_NUMBER
docker push $POSTGRESREPO

if [ "$?" != 0 ]; then
   exit "$?"
fi