package org.interview.server.controller;

import org.interview.common.Message;
import org.interview.server.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        logger.info("Request received to get all messages");
        List<Message> messages = messageService.findAllMessages();
        logger.info("Returning {} messages", messages.size());
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody Message message) {
        logger.info("Received message from middleware: {}", message);

        try {
            messageService.saveMessage(message);
            return ResponseEntity.ok("Message processed successfully");
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
            return ResponseEntity.internalServerError().body("Error processing message: " + e.getMessage());
        }
    }
}
