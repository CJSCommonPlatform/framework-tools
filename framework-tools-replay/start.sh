#!/usr/bin/env bash

# Drop tables
java -jar notification-viewstore-liquibase-1.0.12-SNAPSHOT.jar --url=jdbc:postgresql://localhost:5432/notificationviewstore --username=notification --password=notification --logLevel=info dropAll

# Update view store tables
java -jar notification-viewstore-liquibase-1.0.12-SNAPSHOT.jar --url=jdbc:postgresql://localhost:5432/notificationviewstore --username=notification --password=notification --logLevel=info update

# Update event buffer tables
mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:event-buffer-liquibase:0.33.0:jar
java -jar target/event-buffer-liquibase-0.33.0.jar --url=jdbc:postgresql://localhost:5432/notificationviewstore --username=notification --password=notification --logLevel=info update

# Replay event streams
java -jar target/framework-tools-replay-1.0.0-SNAPSHOT-swarm.jar replay -c standalone-ds.xml -l notification-event-listener-1.0.12-SNAPSHOT.war