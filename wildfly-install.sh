#!/bin/bash

set -e

WILDFLY_VERSION="26.1.3.Final"
WILDFLY_FILENAME="wildfly-${WILDFLY_VERSION}.tar.gz"
WILDFLY_DOWNLOAD_URL="https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/${WILDFLY_FILENAME}"
WILDFLY_HOME="/opt/wildfly"
DOWNLOAD_DIR="/tmp"

echo "Installing WildFly ${WILDFLY_VERSION}..."


if [ ! -f "${DOWNLOAD_DIR}/${WILDFLY_FILENAME}" ]; then
    echo "Downloading WildFly ${WILDFLY_VERSION}..."
    curl -L -o "${DOWNLOAD_DIR}/${WILDFLY_FILENAME}" "${WILDFLY_DOWNLOAD_URL}"
    echo "Download completed."
else
    echo "WildFly archive already exists at ${DOWNLOAD_DIR}/${WILDFLY_FILENAME}. Skipping download."
fi

systemctl stop wildfly.service 2>/dev/null || true
rm -rf "${WILDFLY_HOME}" "wildfly-${WILDFLY_VERSION}" || true

echo "Extracting WildFly to ${WILDFLY_HOME}..."
tar -xzf "${DOWNLOAD_DIR}/${WILDFLY_FILENAME}"
mv "wildfly-${WILDFLY_VERSION}" "${WILDFLY_HOME}"

chown -R root:root "${WILDFLY_HOME}"
chmod -R 755 "${WILDFLY_HOME}"

echo "WildFly ${WILDFLY_VERSION} has been installed to ${WILDFLY_HOME}"

echo "WildFly installation completed successfully."
