FROM amazoncorretto:11-alpine-jdk
ARG JAR_FILE=target/*.jar
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081
COPY ${JAR_FILE} share-it-gateway.jar
ENTRYPOINT ["java","-jar","/share-it-gateway.jar"]