FROM openjdk:10

ENV SCALA_VERSION 2.12.6
RUN wget https://downloads.lightbend.com/scala/2.12.6/scala-2.12.6.deb
RUN dpkg -i scala-$SCALA_VERSION.deb
RUN rm scala-$SCALA_VERSION.deb

ENV SBT_VERSION 1.2.3
RUN wget https://dl.bintray.com/sbt/debian/sbt-1.2.3.deb
RUN dpkg -i sbt-$SBT_VERSION.deb
RUN rm sbt-$SBT_VERSION.deb

ENV FINCH_HOME /finch_app
COPY . /$FINCH_HOME
WORKDIR $FINCH_HOME

RUN sbt clean
RUN sbt compile
RUN sbt package

ENV CLASSPATH $FINCH_HOME/target/streams/compile/dependencyClasspath/\$global/streams/export
CMD java -cp target/scala-2.12/finch_app_2.12-0.1.jar:$(cat $CLASSPATH) Main
