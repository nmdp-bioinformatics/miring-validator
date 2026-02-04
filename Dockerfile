ARG DOCKERIO_PROXY
FROM ${DOCKERIO_PROXY}/library/tomcat:9.0.20-jre8-alpine

RUN cd /usr/local/tomcat/webapps \
    && rm -rf ROOT/ docs/ examples/ host-manager/ manager/

WORKDIR /app

COPY target/MiringValidator.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]


