FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar ewm-app.jar
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081
ENTRYPOINT ["java","-jar","/ewm-app.jar"]