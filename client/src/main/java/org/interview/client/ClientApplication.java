package org.interview.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.interview.common.Message;

import javax.jms.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Properties;

public class ClientApplication {
    private final Properties properties;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public ClientApplication() {
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream localInput = Files.newInputStream(Paths.get("client.properties"))) {
            props.load(localInput);
            return props;
        } catch (IOException ignored) {
            // Ignore and try classpath
        }

        try (InputStream input = ClientApplication.class.getClassLoader()
                .getResourceAsStream("client.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find client.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
        return props;
    }

    public static void main(String[] args) {
        ClientApplication client = new ClientApplication();
        try {
            client.initialize();
            client.sendMessage(args[0]);
        } catch (Exception e) {
            System.err.println("Error in application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.cleanup();
        }
    }

    public void initialize() throws JMSException {
        String brokerUrl = properties.getProperty("broker.url");
        String brokerUsername = properties.getProperty("broker.username");
        String brokerPassword = properties.getProperty("broker.password");
        String topicName = properties.getProperty("topic.name");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection(brokerUsername, brokerPassword);
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic messageTopic = session.createTopic(topicName);
        producer = session.createProducer(messageTopic);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        connection.start();
    }

    public void sendMessage(String messageString) throws JMSException {
        Message message = new Message(messageString, LocalDateTime.now());
        sendMessage(message);

        System.out.println("Message sent: " + message);
        System.out.println();
    }

    private void sendMessage(Message message) throws JMSException {
        try {
            ObjectMessage objectMessage = session.createObjectMessage(message);
            
            producer.send(objectMessage);
        } catch (Exception e) {
            throw new JMSException("Failed to send message: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}