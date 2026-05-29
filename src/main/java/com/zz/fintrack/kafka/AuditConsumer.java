package com.zz.fintrack.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuditConsumer {
    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    @KafkaListener(topics = "transaction-events", groupId = "fintrack-audit-group")
    public void consume(String message) {
        log.info("\n========================================");
        log.info("📢 AUDIT LOG EVENT RECEIVED VIA KAFKA:");
        log.info("{}", message);
        log.info("========================================\n");
    }
}
