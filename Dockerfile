#FROM ubuntu:14.04
FROM tomcat:8.0-jre8
MAINTAINER Ben Matern <bmatern@nmdp.org>

#Backend apt-get stuff
RUN apt-get -y update 
#RUN apt-get dist-upgrade -qy
#RUN apt-get install -y software-properties-common python-software-properties
RUN apt-get install -y software-properties-common

#Install Java JDK
RUN add-apt-repository -y ppa:openjdk-r/ppa
RUN apt-get install -y openjdk-8-jdk

#Install maven
RUN apt-get install -y maven

#Install Git
RUN apt-get install -y git

#Clone Github Project Site
RUN cd /opt && git clone https://github.com/bmatern-nmdp/MiringValidator

#Expose my port
EXPOSE 8080

#Build and Run Miring Validator
RUN cd /opt/MiringValidator && mvn clean install tomcat7:run-war



