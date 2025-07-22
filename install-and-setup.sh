#!/bin/bash

set -e

echo "Starting installation and setup process..."

RELEASE_DIR="/opt/overengineered-project"

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must be run as root or with sudo privileges"
    exit 1
fi

echo "Updating system packages..."
yum update -y

#echo "Installing OpenJDK 8..."
#yum install -y java-1.8.0-openjdk-devel
#
#echo "Installing Maven..."
#yum install -y maven

# For Amazon Linux 2, we use Amazon Corretto for Java 8 and Maven
echo "Installing Corretto Maven and Java 8..."
yum install -y maven-amazon-corretto8

echo "Installing build dependencies for t21-formatter..."
yum install -y cmake gcc-c++ make

echo "Creating release directory: ${RELEASE_DIR}"
rm -rf "${RELEASE_DIR}" || true
mkdir -p "${RELEASE_DIR}"
chmod 755 "${RELEASE_DIR}"

if [ -f "./wildfly-install.sh" ]; then
    echo "Running Wildfly installation script..."
    chmod +x ./wildfly-install.sh
    bash wildfly-install.sh
else
    echo "wildfly-install.sh not found"
    exit 1
fi

if [ -f "./wildfly-setup.sh" ]; then
    echo "Running Wildfly setup script..."
    chmod +x ./wildfly-setup.sh
    bash wildfly-setup.sh
else
    echo "wildfly-setup.sh not found"
    exit 1
fi

echo "Building the project with Maven..."
mvn clean package

echo "Copying JAR files to release directory..."
find client/target -name "*-with-dependencies.jar" -exec cp {} "${RELEASE_DIR}" \;
find middleware/target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" -not -name "*-tests.jar" -exec cp {} "${RELEASE_DIR}" \;
find server/target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" -not -name "*-tests.jar" -exec cp {} "${RELEASE_DIR}" \;

if [ -f "t21-formatter/build.sh" ]; then
    echo "Building T21-formatter..."
    chmod +x t21-formatter/build.sh
    bash t21-formatter/build.sh
else
    echo "t21-formatter/build.sh not found"
    exit 1
fi

echo "Copying t21-formatter files to release directory..."
if [ -f "t21-formatter/build/t21_formatter" ]; then
    cp t21-formatter/build/t21_formatter "${RELEASE_DIR}/"
    cp t21-formatter/input.csv "${RELEASE_DIR}/"
    cp t21-formatter/manufacturers.csv "${RELEASE_DIR}/"
    cp t21-formatter/models.csv "${RELEASE_DIR}/"
    chmod 755 "${RELEASE_DIR}/t21_formatter"
else
    echo "t21-formatter binary not found after build"
    exit 1
fi

cp run-h2.sh "${RELEASE_DIR}/run-h2.sh"

echo "Setting permissions on release files..."
chmod -R 755 "${RELEASE_DIR}"

echo "Installation and setup completed successfully!"
echo "The application has been deployed to ${RELEASE_DIR}"
