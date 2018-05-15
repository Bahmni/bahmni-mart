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
### Deployment Steps
To deploy/install bahmni-mart using docker follow the steps given below
* install docker-1.7.1
* install docker-compose-1.5.2
* download **bahmni-mart/scripts/install.sh** from github
* run ```sh install.sh```
* download **bahmni-mart/docker-compose.yml** from github
* Change any config if needed. Bahmni-mart related config files will be present in **/opt/bahmni-mart/conf**. Change **application-docker.properties** if you want modify given parameters to bahmni-mart application or Spring cloud dataflow server
* run ```docker-compose up -d```

---
### Commands to Remember
* ```docker-compose up -d``` (Fetch docker images from remote if images are not present in local and run in headless mode)
* ```docker ps -a``` (Check status of docker containers)
* ```docker logs -f postgres-server``` (Check logs of postgres-server container)
* ```docker logs -f dataflow-server``` (Check logs of dataflow-server container)
* ```docker-compose stop``` (Stop all docker containers)
* ```docker-compose rm -f``` (Remove all stopped containers)
* ```docker start <CONTAINER ID>``` (Start specific docker container)
* ```docker exec -it postgres-server psql -Uanalytics``` (Access PSQL database of postgres-server container)
* ```curl -X POST <HOST NAME>:9393/tasks/executions\?name\=create-bahmni-mart``` (Once Spring Cloud Dataflow Server is up, you can launch task using UI. But if you want to do it from command line you can run this command. It will launch **create-bahmni-mart** task)
* ```tail -100f /var/log/bahmni-mart/bahmmni-mart.log``` (Log of **create-bahmni-mart** task will be present in **/var/log/bahmni-mart/bahmni-mart.log** of your host machine. Use this command to get last 100 lines of the file)