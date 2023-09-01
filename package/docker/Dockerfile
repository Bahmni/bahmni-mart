FROM amazoncorretto:11

COPY build/libs/bahmni-mart*.jar /bahmni-mart/app.jar
COPY package/docker/files/ /bahmni-mart
COPY conf /bahmni-mart/conf

RUN chmod +x /bahmni-mart/setup.sh

RUN yum -y install crontabs

CMD ["sh", "./bahmni-mart/setup.sh"]