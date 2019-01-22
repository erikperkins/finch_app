FROM openjdk:10

ENV SCALA_VERSION 2.12.6
RUN wget https://downloads.lightbend.com/scala/2.12.6/scala-2.12.6.deb
RUN dpkg -i scala-$SCALA_VERSION.deb
RUN rm scala-$SCALA_VERSION.deb

ENV SBT_VERSION 1.2.3
RUN wget https://dl.bintray.com/sbt/debian/sbt-1.2.3.deb
RUN dpkg -i sbt-$SBT_VERSION.deb
RUN rm sbt-$SBT_VERSION.deb

ENV CLOUDBERRY_HOME /cloudberry
COPY . /$CLOUDBERRY_HOME
WORKDIR $CLOUDBERRY_HOME

RUN wget http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.7.4/aws-java-sdk-1.7.4.jar
RUN wget http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.7.1/hadoop-aws-2.7.1.jar
RUN wget http://central.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar

RUN sbt clean
RUN sbt compile
RUN sbt package

ENV CLASSPATH $CLOUDBERRY_HOME/target/streams/compile/dependencyClasspath/\$global/streams/export
ENV AWS_JAVA_SDK $CLOUDBERRY_HOME/aws-java-sdk-1.7.4.jar
ENV HADOOP_S3 $CLOUDBERRY_HOME/hadoop-aws-2.7.1.jar
ENV POSTGRESQL $CLOUDBERRY_HOME/postgresql-42.2.5.jar

CMD java -cp target/scala-2.12/cloudberry_2.12-0.1.jar:$(cat $CLASSPATH):$AWS_JAVA_SDK:$HADOOP_S3:$POSTGRESQL Main
