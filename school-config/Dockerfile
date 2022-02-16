FROM openjdk:11
EXPOSE 7000
COPY ./target/school-config-1.0-SNAPSHOT.jar /usr/src/school/config/
COPY ./target/config.json /usr/src/school/config/
WORKDIR /usr/src/school/config
ENTRYPOINT ["java", "-jar", "school-config-1.0-SNAPSHOT.jar"]