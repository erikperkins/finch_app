FROM openjdk:10

ENV FINCH_HOME /finch_app

COPY . /$FINCH_HOME
WORKDIR $FINCH_HOME
