
# docker build -t swint .
# docker run -d -p 8888:8888 --network swint-network --name swint-was swint-was:e18f894
# docker exec -it swint /bin/bash

FROM centos:7

VOLUME /app/data

# 1. tools
RUN yum install -y curl unzip wget net-tools


# 2. JAVA
# install JAVA-11
RUN yum update -y && \
        yum install -y java-11-openjdk-devel.x86_64
# set the JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-11.0.19.0.7-1.el7_9.x86_64
RUN export JAVA_HOME


# 3. GRADLE
# set the enviroment for GRADLE
ENV GRADLE_VERSION=8.1.1
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
# install GRADLE
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -d /opt/gradle gradle-${GRADLE_VERSION}-bin.zip
# set the GRADLE_HOME
RUN export GRADLE_HOME
ENV PATH $PATH:$GRADLE_HOME/bin
RUN export PATH


# 4. Firewalld
# set the firewall for SW port
#RUN firewall-cmd --zone=public --add-port=${SW_PORT}/tcp --permanent
#RUN firewall-cmd --reload


# 5. ENV for source code build
# set the enviroment for SW
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=postgres
ENV DB_USER=postgres
ENV DB_PASS=postgres
ENV LOCAL_HOST=localhost
ENV LOCAL_PORT=8888

ENV AUTH_HOST=https://auth.dev.dranswer-g.co.kr
ENV USER_REALM=service-user-dev
ENV USER_CLIENT=login
ENV USER_SECRET=JG6eaVSJVzbUp5Sgp7MAyByrraZX7xNC
ENV SYSTEM_REALM=service-system-dev
ENV SYSTEM_CLIENT=swint
ENV SYSTEM_SECRET=K91G3XhKY3z2qussprBEAC24cksv0qNk

ENV CMAN_HOST=http://115.85.182.6:30330
ENV LOCAL_HOST=http://localhost:8888/callback


# 6. Copy and Build
# Copy the Gradle project source code
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build

CMD ["java", "-jar", "/app/build/libs/service_portal-0.0.1-SNAPSHOT.jar"]

EXPOSE 8888
