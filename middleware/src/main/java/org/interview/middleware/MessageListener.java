package org.interview.middleware;

import org.interview.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private final OverengineeredService overengineeredService;

    @Autowired
    public MessageListener(OverengineeredService overengineeredService) {
        this.overengineeredService = overengineeredService;
    }

    @JmsListener(destination = "message-topic", 
                subscription = "messageServerSub")
    public void receiveMessage(Message message) {
        logger.info("Received message: {}", message);

        boolean success = overengineeredService.queueMessage(message);

        if (success) {
            logger.info("Message successfully queued for processing: {}", message.getMessage());
        } else {
            logger.warn("Failed to queue message for processing: {}", message.getMessage());
        }
    }
}