# bahmni-mart

[![Build Status](https://travis-ci.org/bahmni-msf/bahmni-mart.svg?branch=master)](https://travis-ci.org/bahmni-msf/bahmni-mart) &nbsp;&nbsp;[![Codacy Badge](https://api.codacy.com/project/badge/Grade/67a328ac886445bf88e808becc35dece)](https://www.codacy.com/app/sumanmaity112/bahmni-mart?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=bahmni-msf/bahmni-mart&amp;utm_campaign=Badge_Grade)

Standalone Batch Application based on spring-batch. This application will create various table in **analytics** DB like Patient Information, Program Enrollment Information, Drug Orders (TB and Non-TB), Various forms filled by the users (Observation Templates), Bacteriology forms information

---
## Setting up Dev Environment

### Prerequisites
* **java 8**
* **docker** (version 1.7.1)
    * For **CentOS 6.7** install **docker-io**. To install it run follow the steps presents [here](https://centos.pkgs.org/6/epel-x86_64/docker-io-1.7.1-2.el6.x86_64.rpm.html).
* **docker-compose** (version 1.5.2)
    * To install it in **CentOS 6.7** run the following commands
    
        ```bash
            sudo wget http://github.com/docker/compose/releases/download/1.5.2/docker-compose-`uname -s`-`uname -m` -O /usr/bin/docker-compose
            chmod +x /usr/bin/docker-compose
        ```
### Setting up Git Hooks
To setup git hooks please run the following command
* ```sh scripts/dev/setup.sh```

---
### Build 
#### JAR
To build JAR run the following command
* ```./gradlew clean build```
 
#### Bahmni mart docker image
To build bahmni mart docker image run the following command
* ```docker build -f JavaDockerfile -t anallytics/bahmni-mart .```
 
#### Postgres docker image
To build postgres docker image for bahmni mart run the following command
* ```docker build -f PostgresqlDockerfile -t anallytics/postgres .```

---
### Checks/ Verifications
#### Test
To run tests run the following command
* ```./gradlew test```

#### Coverage
To check code coverage run the following command
* ```./gradlew clean jacocoTestCoverageVerification```
 
#### Style
To check code style run the following command
* ```./gradlew clean checkstyleMain checkstyleTest```

#### All
To run all chesks present in bahmni mart run the following command
* ```./gradlew clean check```

---
### Commands to Remember
* ```docker-compose up -d``` (Fetch docker images from remote if images are not present in local and run in headless mode)
* ```docker ps -a``` (Check status of docker containers)
* ```docker logs -f mart``` (Check logs of bahmni-mart container)
* ```docker logs -f postgres-server``` (Check logs of postgres-server container)
* ```docker-compose stop``` (Stop all docker containers)
* ```docker-compose rm -f``` (Remove all stopped containers)
* ```docker start <CONTAINER ID>``` (Start specific docker container)
* ```docker exec -it postgres-server psql -Uanalytics``` (Access PSQL database of postgres-server container)
 