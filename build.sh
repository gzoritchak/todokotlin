#!/bin/bash

SERVER=$(basename `pwd`)

#docker run -u root --rm \
#    -v ~/.gradle:/home/gradle/.gradle \
#    -v ~/.m2:/root
#    -v "$PWD":/home/gradle/project \
#    -w /home/gradle/project \
#    gradle:4.7-jdk8 gradle build -x test

./gradlew build -x test

docker build -t $SERVER .

