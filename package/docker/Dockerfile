FROM amazoncorretto:8

COPY build/libs/bahmni-mart*.jar /bahmni-mart/app.jar
COPY package/docker/files/ /bahmni-mart
COPY conf /bahmni-mart/conf

RUN chmod +x /bahmni-mart/setup.sh

RUN yum -y update && yum -y install crontabs && yum clean all

CMD ["sh", "./bahmni-mart/setup.sh"]