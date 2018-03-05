#Fetching base image from repository
FROM centos:6.7
MAINTAINER The CentOS Project <cloud-ops@centos.org>

ENV java_runtime jre
ENV java_version 8u131
ENV java_build b11

#Installing Dependencies
RUN yum install http://yum.postgresql.org/9.2/redhat/rhel-6-x86_64/pgdg-centos92-9.2-7.noarch.rpm -y
RUN yum install postgresql92-server postgresql92-contrib cronie sudo wget initscripts -y

# Downloading & Config Java 8
RUN wget --no-check-certificate --no-cookies --header 'Cookie:oraclelicense=accept-securebackup-cookie' "http://download.oracle.com/otn-pub/java/jdk/$java_version-$java_build/d54c1d3a095b4ff2b6607d096fa80163/$java_runtime-$java_version-linux-x64.rpm" -O /tmp/jdk-8-linux-x64.rpm
RUN yum -y install /tmp/jdk-8-linux-x64.rpm
RUN alternatives --install /usr/bin/java jar /usr/java/latest/bin/java 200000
RUN alternatives --install /usr/bin/javaws javaws /usr/java/latest/bin/javaws 200000
RUN alternatives --install /usr/bin/javac javac /usr/java/latest/bin/javac 200000

#Adding Config
ADD conf/bahmni-mart-postgres.conf /opt/bahmni-mart/conf/bahmni-mart-postgres.conf
RUN chmod +x /opt/bahmni-mart/conf/bahmni-mart-postgres.conf

#Adding postInstall script
ADD scripts/postinstall.sh /opt/postgresql/scripts/postinstall.sh
RUN chmod +x /opt/postgresql/scripts/postinstall.sh
RUN /opt/postgresql/scripts/postinstall.sh

#Adding bahmni-analytics script
ADD scripts/bahmni-mart /opt/bahmni-mart/scripts/bahmni-mart.sh
RUN chmod +x /opt/bahmni-mart/scripts/bahmni-mart.sh

#Adding wait-for-postgres.sh script
ADD scripts/wait-for-postgres.sh /opt/bahmni-mart/scripts/wait-for-postgres.sh
RUN chmod +x /opt/bahmni-mart/scripts/wait-for-postgres.sh

#Copying Jar to Docker
ADD build/libs/bahmni-mart-*.jar bahmni-mart.jar

EXPOSE 5432

RUN sh -c 'touch /bahmni-mart.jar'

ENTRYPOINT sysctl -w kernel.shmmax=17179869184;service postgresql-9.2 start;/opt/bahmni-mart/scripts/wait-for-postgres.sh; sh /opt/bahmni-mart/conf/bahmni-mart.conf; /opt/bahmni-mart/scripts/bahmni-mart.sh ;/bin/bash


