FROM dockerfile/java:oracle-java8

ADD build/libs/file-repository-0.0.1-SNAPSHOT.jar /data/file-repository-0.0.1-SNAPSHOT.jar

ADD config.yml /data/config.yml

CMD java -jar /data/file-repository-0.0.1-SNAPSHOT.jar server /data/config.yml

EXPOSE 8181
EXPOSE 8182
