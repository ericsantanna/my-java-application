package org.interview.server.service;

import org.interview.common.Message;
import org.interview.server.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public void saveMessage(Message message) {
        logger.info("Saving message: {}", message);

        if (message.getLastUpdate() == null) {
            message.setLastUpdate(LocalDateTime.now());
        }

        messageRepository.save(message);
        logger.info("Saved message: {}", message);
    }

    /**
     * Retrieves all messages from the database
     * @return List of all messages
     */
    @Transactional(readOnly = true)
    public List<Message> findAllMessages() {
        logger.info("Finding all messages");
        return messageRepository.findAll();
    }
}
