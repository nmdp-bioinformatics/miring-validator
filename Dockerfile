#FROM ubuntu:14.04
FROM tomcat:8.0-jre8
MAINTAINER Ben Matern <bmatern@nmdp.org>

#Backend apt-get stuff
#RUN apt-get -y update 
#&& apt-get install -y software-properties-common python-software-properties

#Install Java
#RUN add-apt-repository -y ppa:openjdk-r/ppa
#&& apt-get -y update 
#&& apt-get install -y openjdk-8-jre-headless

#Tomcat
#RUN apt-get install -y tomcat8

EXPOSE 8080

