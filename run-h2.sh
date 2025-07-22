#!/bin/bash

H2_VERSION=1.4.200

mkdir -p data

if [ ! -f "data/h2-$H2_VERSION.jar" ]; then
  echo "Downloading H2 version ${H2_VERSION}..."
  wget --no-check-certificate -P data "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar"
fi

echo "Starting H2 TCP server on port 9092..."
java -cp "data/h2-${H2_VERSION}.jar" org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -ifNotExists
