FROM openjdk:8
COPY /./../out/production/EdgeServer/* /tmp
WORKDIR /tmp
EXPOSE 8001
ENTRYPOINT ["java", "BackendServer"]
