FROM openjdk:17-alpine

RUN apk add --no-cache bash

ARG JAR_FILE=build/libs/*.dadok.jar

RUN echo

COPY ${JAR_FILE} dadok.jar

ENV SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE} \
DATASOURCE_URL: ${DATASOURCE_URL} \
DATASOURCE_USERNAME: ${DATASOURCE_USERNAME} \
DATASOURCE_PASSWORD: ${DATASOURCE_PASSWORD} \
DRIVER_CLASS_NAME: ${DRIVER_CLASS_NAME} \
HIBERNATE_DDL_AUTO: ${HIBERNATE_DDL_AUTO} \
HIBERNATE_DIALECT: ${HIBERNATE_DIALECT} \
HIBERNATE_FORMAT_SQL: ${HIBERNATE_FORMAT_SQL} \
HIBERNATE_SHOW_SQL: ${HIBERNATE_SHOW_SQL} \
HIBERNATE_DEFAULT_BATCH_FETCH_SIZE: ${HIBERNATE_DEFAULT_BATCH_FETCH_SIZE} \
ENABLE_JPA_OPEN_IN_VIEW: ${ENABLE_JPA_OPEN_IN_VIEW} \
DATABASE_NAME: ${DATABASE_NAME} \
ENABLE_P6SPY: ${ENABLE_P6SPY} \
JWT_SECRET_KEY: ${JWT_SECRET_KEY} \
KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID} \
KAKAO_CLIENT_SECRET: ${KAKAO_CLIENT_SECRET} \
KAKAO_REDIRECT_PATH: ${KAKAO_REDIRECT_PATH} \
NAVER_CLIENT_ID: ${NAVER_CLIENT_ID} \
NAVER_CLIENT_SECRET: ${NAVER_CLIENT_SECRET} \
NAVER_REDIRECT_PATH: ${NAVER_REDIRECT_PATH} \
ACCESS_TOKEN_EXPIRE_SECONDS: ${ACCESS_TOKEN_EXPIRE_SECONDS} \
SERVER_HOST: ${SERVER_HOST}

ENTRYPOINT ["java", \
"-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
"-jar", "/dadok.jar"]