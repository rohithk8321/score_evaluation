FROM openjdk:11

COPY target/score_evaluation-0.0.1-SNAPSHOT.jar score_evaluation-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/score_evaluation-0.0.1-SNAPSHOT.jar"]