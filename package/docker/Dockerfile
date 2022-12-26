FROM adoptopenjdk/openjdk8:jre8u345-b01

COPY build/libs/bahmni-mart*.jar /bahmni-mart/app.jar
COPY package/docker/files/ /bahmni-mart
COPY conf /bahmni-mart/conf

RUN chmod +x /bahmni-mart/setup.sh

RUN apt-get update && apt-get -y install cron

CMD ["sh", "./bahmni-mart/setup.sh"]