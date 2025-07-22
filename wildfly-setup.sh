#!/bin/bash

set -e

WILDFLY_HOME="/opt/wildfly"
WILDFLY_USER="wildfly"
WILDFLY_GROUP="wildfly"

echo "Setting up WildFly..."

if [ ! -d "${WILDFLY_HOME}" ]; then
    echo "WildFly installation not found at ${WILDFLY_HOME}. Please run wildfly-install.sh first."
    exit 1
fi

if [ ! -f "wildfly-users.env" ]; then
    echo "Please create a file wildfly-users.env setting the variables WILDFLY_MANAGEMENT_USER, WILDFLY_MANAGEMENT_PASSWORD, WILDFLY_APPLICATION_USER, and WILDFLY_APPLICATION_PASSWORD."
    exit 1
fi

source wildfly-users.env

if ! getent group "${WILDFLY_GROUP}" > /dev/null; then
    echo "Creating group: ${WILDFLY_GROUP}"
    groupadd -r "${WILDFLY_GROUP}"
fi

if ! getent passwd "${WILDFLY_USER}" > /dev/null; then
    echo "Creating user: ${WILDFLY_USER}"
    useradd -r -g "${WILDFLY_GROUP}" -d "${WILDFLY_HOME}" -s /sbin/nologin "${WILDFLY_USER}"
fi

echo "Setting permissions..."
chown -R "${WILDFLY_USER}:${WILDFLY_GROUP}" "${WILDFLY_HOME}"

mkdir -p /var/run/wildfly

cat > /etc/systemd/system/wildfly.service << 'EOF'
[Unit]
Description=WildFly Application Server
After=network.target

[Service]
Type=simple
User=wildfly
Group=wildfly
ExecStart=/opt/wildfly/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0
StandardOutput=null
LimitNOFILE=102642
PIDFile=/var/run/wildfly/wildfly.pid

[Install]
WantedBy=multi-user.target
EOF

mkdir -p "${WILDFLY_HOME}/standalone/deployments"
chown -R "${WILDFLY_USER}:${WILDFLY_GROUP}" "${WILDFLY_HOME}/standalone/deployments"

systemctl daemon-reload
systemctl enable wildfly.service

echo "Setting up WildFly management and application users..."

bash "${WILDFLY_HOME}/bin/add-user.sh" -u "${WILDFLY_MANAGEMENT_USER}" -p "${WILDFLY_MANAGEMENT_PASSWORD}" -g guest
bash "${WILDFLY_HOME}/bin/add-user.sh" -a -u "${WILDFLY_APPLICATION_USER}" -p "${WILDFLY_APPLICATION_PASSWORD}" -g guest

systemctl start wildfly.service
echo "WildFly service has been started"

echo "Waiting for WildFly to start..."
sleep 10

if systemctl is-active --quiet wildfly; then
    echo "WildFly is running successfully"
else
    echo "WARNING: WildFly service is not running. Please check the logs: journalctl -u wildfly"
fi

echo "Configuring WildFly JMS server..."

"${WILDFLY_HOME}/bin/jboss-cli.sh" "--user=${WILDFLY_MANAGEMENT_USER}" "--password=${WILDFLY_MANAGEMENT_PASSWORD}" --file=setup-messaging.cli

echo "WildFly setup completed successfully."
