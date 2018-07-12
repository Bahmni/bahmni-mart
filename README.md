# bahmni-mart

[![Build Status](https://travis-ci.org/bahmni-msf/bahmni-mart.svg?branch=master)](https://travis-ci.org/bahmni-msf/bahmni-mart) &nbsp;&nbsp;[![Codacy Badge](https://api.codacy.com/project/badge/Grade/67a328ac886445bf88e808becc35dece)](https://www.codacy.com/app/sumanmaity112/bahmni-mart?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=bahmni-msf/bahmni-mart&amp;utm_campaign=Badge_Grade)

Standalone Batch Application based on spring-batch. This application will create various table in **analytics** DB like Patient Information, Program Enrollment Information, Drug Orders (TB and Non-TB), Various forms filled by the users (Observation Templates), Bacteriology forms information

---
## Setting up Dev Environment

### Prerequisites
* **java 8**

### Setting up Git Hooks and Test DB
To setup git hooks please run the following command
```bash
sh scripts/dev/setup.sh
```

---
### Build 
#### JAR
To build JAR run the following command
```bash
./gradlew clean build
```

---
### Checks/ Verifications
#### Test
To run tests run the following command
```bash
./gradlew test
```

#### Coverage
To check code coverage run the following command
```bash
./gradlew clean jacocoTestCoverageVerification
```
 
#### Style
To check code style run the following command
```bash
./gradlew clean checkstyleMain checkstyleTest
```

#### All
To run all chesks present in bahmni mart run the following command
```bash
./gradlew clean check
```

---
## Deployment Steps
### Prerequisites
* **Ansible Playbook (v2.2)**. To install it, run the following command 
    ```bash
    yum install ansible-2.2.0.0
    ```
### Installation Steps

To install bahmni-mart follow the steps given below
* Download Bahmni-mart playbook from Github
    ```bash
    wget -O /tmp/bahmni-mart-playbook.zip https://github.com/bahmni-msf/bahmni-mart-playbook/archive/master.zip && unzip -o /tmp/bahmni-mart-playbook.zip -d /tmp && sudo rm -rf /etc/bahmni-mart-playbook && sudo mv /tmp/bahmni-mart-playbook-master /etc/bahmni-mart-playbook && rm -rf /tmp/bahmni-mart-playbook.zip
    ```
* Update **/etc/bahmni-mart-playbook/inventories/bahmni-mart** inventory file as part your requirement
* Update the values presents in **/etc/bahmni-mart-playbook/setup.yml** inventory file as part your requirement
* Install **Bahmni-mart** application
    ```bash
    ansible-playbook -i /etc/bahmni-mart-playbook/inventories/bahmni-mart /etc/bahmni-mart-playbook/all.yml --extra-vars '@/etc/bahmni-mart-playbook/setup.yml'
    ```
* Update **bahmni-mart** config. The config will be present in **/var/www/bahmni_config/bahmni-mart/bahmni-mart.json** 

### Access the Application
**Bahmni-mart** application should be installed on you system. To create flattened DB from CLI, run ```bahmni-mart``` command.

You can access **Bahmni-mart** and other application from browser also.

|Application | URL | Comment | 
|:-----------|:------|:---------|
|Bahmni mart  |http://<HOST NAME>/dashboard/#/tasks/definitions/launch/create-bahmni-mart| Only if bahmni-mart-scdf is installed|
|Metabase|http://<HOST NAME>:9003|Only if metabase is installed|

---
### Commands to Remember
* ```docker ps -a``` (Check status of docker containers)
* ```docker logs -f metabase``` (Check logs of metabase container)
* ```docker logs -f dataflow-server``` (Check logs of dataflow-server container)
* ```docker stop <CONATAINER NAME>``` (Stop specific docker container)
* ```docker start <CONTAINER NAME>``` (Start specific docker container)
* ```curl -X POST <HOST NAME>:9393/tasks/executions\?name\=create-bahmni-mart``` (Once Spring Cloud Dataflow Server is up, you can launch task using UI. But if you want to do it from command line you can run this command. It will launch **create-bahmni-mart** task)
* ```tail -100f /var/log/bahmni-mart/bahmmni-mart.log``` (Log of **create-bahmni-mart** task will be present in **/var/log/bahmni-mart/bahmni-mart.log** of your host machine. Use this command to get last 100 lines of the file)