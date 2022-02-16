FROM openjdk:11
EXPOSE 7005
COPY ./target/school-users-1.0-SNAPSHOT.jar /usr/src/school/users/
# COPY ./out/artifacts/school_users_jar/school-users.jar  /usr/src/school/users/
WORKDIR /usr/src/school/users
ENTRYPOINT ["java", "-jar", "school-users-1.0-SNAPSHOT.jar"]
# ENTRYPOINT ["java", "-jar", "school-users.jar"]
