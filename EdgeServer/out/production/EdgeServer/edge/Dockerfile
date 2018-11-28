FROM openjdk:8
COPY /./../out/production/EdgeServer/* /tmp
WORKDIR /tmp
EXPOSE 8000
ENTRYPOINT ["java", "EdgeServer"]
