package com.zz.fintrack.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionEvent(String message) {
        kafkaTemplate.send("transaction-events", message);
    }
}
