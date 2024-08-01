FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar statistic-app.jar
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9091
ENTRYPOINT ["java","-jar","/statistic-app.jar"]