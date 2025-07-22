package org.interview.middleware;

import org.interview.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class OverengineeredService {

    private static final Logger logger = LoggerFactory.getLogger(OverengineeredService.class);

    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread processingThread;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.endpoint.url}")
    private String apiEndpointUrl;

    public boolean queueMessage(Message message) {
        if (message == null) {
            logger.warn("Attempted to queue null message");
            return false;
        }

        try {
            messageQueue.put(message);
            logger.info("Added message to queue: {}", message);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while adding message to queue", e);
            return false;
        }
    }

    @PostConstruct
    public void start() {
        if (running.compareAndSet(false, true)) {
            processingThread = new Thread(() -> {
                logger.info("Message processing thread started");
                while (running.get()) {
                    try {
                        Message message = messageQueue.take();
                        sendMessageToApi(message);
                    } catch (InterruptedException e) {
                        logger.info("Processing thread interrupted");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error("Error processing message from queue", e);
                    }
                }
                logger.info("Message processing thread stopped");
            }, "message-processor");

            processingThread.setDaemon(true);
            processingThread.start();
            logger.info("Message processing service started");
        } else {
            logger.warn("Processing service is already running");
        }
    }

    private void sendMessageToApi(Message message) {
        try {
            logger.info("Sending message to external API: {}", message);
            ResponseEntity<String> response = restTemplate.postForEntity(apiEndpointUrl, message, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully sent message to API: {}", message);
            } else {
                logger.warn("API returned non-success status code: {} for message: {}", 
                        response.getStatusCode(), message);
            }
        } catch (RestClientException e) {
            logger.error("Failed to send message to API: {}", message, e);
        }
    }

    @PreDestroy
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping message processing service");
            if (processingThread != null) {
                processingThread.interrupt();
                try {
                    processingThread.join(5000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for processing thread to stop");
                    Thread.currentThread().interrupt();
                }
            }
            logger.info("Message processing service stopped");
        }
    }
}