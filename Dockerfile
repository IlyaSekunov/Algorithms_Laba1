FROM java:21
COPY target/algorithms_laba1-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]