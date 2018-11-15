FROM openjdk:8
COPY ./EdgeServer/out/production/EdgeServer/ /tmp
WORKDIR /tmp
EXPOSE 8000
ENTRYPOINT ["java", "edge/EdgeServer"]
