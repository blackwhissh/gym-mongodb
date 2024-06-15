package com.epam.hibernate.service.jms;

import com.epam.hibernate.dto.TrainingInfoRequest;
import jakarta.transaction.Transactional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainingInfoSender {
    private static final String TRAINING_INFO_QUEUE = "training.info.queue";
    private final JmsTemplate jmsTemplate;

    public TrainingInfoSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void send(TrainingInfoRequest request) {
        jmsTemplate.convertAndSend(TRAINING_INFO_QUEUE, request);
    }
}
