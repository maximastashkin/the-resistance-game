#base image
FROM openjdk:8-jdk

#env
ENV LOG_LEVEL INFO

ADD build/libs/bot-0.0.1-all.jar /usr/src/bot-0.0.1-all.jar

WORKDIR /usr/src

ENTRYPOINT ["java", "-Dtelegram.token=$TELEGRAM_API_KEY -Dtelegram.webhookUrl=$TELEGRAM_WEBHOOK_URL -Dserver.url=$SERVER_URL -jar", "bot-0.0.1-all.jar"]