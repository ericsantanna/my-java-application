# My Java Application

An overengineered Java application used for an interview.

# Environment setup

## Amazon Linux 2

Run:
```bash
bash install-and-setup.sh
chown -R ec2-user:ec2-user ~/my-java-application
chown -R ec2-user:ec2-user /opt/overengineered-project
```

It is going to:
- Build all java modules
- Install Wildfly and configure a JMS topic
- Copy the t21-formatter and jar files to `/opt/overengineedred-project/` and set permissions
- Run `run-h2.sh` to start the H2 database server

Prerequisites

- Java 8 (JDK 1.8)
- Maven or the included Maven wrapper (`./mvnw`)
- A running JMS broker compatible with ActiveMQ Artemis (default expected at `tcp://localhost:5445`).

## Running the application

I recommend to start 4 ssh sessions, one for the database, one for each module

1. Start the DB:
   ```bash
   cd /opt/overengineered-project
   bash run-h2.sh
   ```

2. Start the server:
   ```bash
   cd /opt/overengineered-project
   java -jar server-0.0.1-SNAPSHOT.jar
   ```

3. Start the middleware:
   ```bash
   cd /opt/overengineered-project
   java -jar middleware-0.0.1-SNAPSHOT.jar
   ```
4. Start the client:
   ```bash
   cd /opt/overengineered-project
   java -jar client-0.0.1-SNAPSHOT-jar-with-dependencies.jar
   ```
   

