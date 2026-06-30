FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y \
        maven \
        docker.io \
        && \
    rm -rf /var/lib/apt/lists/*

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /var/jenkins_home/casc.yaml
COPY jobs/ /var/jenkins_home/jobs/
