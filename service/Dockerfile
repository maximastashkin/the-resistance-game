#base image
FROM openjdk:8-jdk

#env
ENV LOG_LEVEL DEBUG

ADD build/libs/service-0.0.1-all.jar /usr/src/service-0.0.1-all.jar

WORKDIR /usr/src

ENTRYPOINT ["java","-DdataBase.url=$JDBC_URL -DdataBase.user=$JDBC_USER -DdataBase.password=$JDBC_PASSWORD -jar", "service-0.0.1-all.jar"]