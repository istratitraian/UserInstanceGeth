FROM openjdk:8
ADD target/userWeb3jInstance-0.0.1-SNAPSHOT.jar userWeb3jInstance-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "userWeb3jInstance-0.0.1-SNAPSHOT.jar"]
