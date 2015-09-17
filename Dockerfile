FROM java:8

ADD libs/girkit-api-server-all.jar /opt/girkit-api-server/girkit-api-server-all.jar
EXPOSE 5050
WORKDIR /opt/girkit-api-server/
ENTRYPOINT ["java", "-Dratpack.port=5050", "-jar", "girkit-api-server-all.jar"]
