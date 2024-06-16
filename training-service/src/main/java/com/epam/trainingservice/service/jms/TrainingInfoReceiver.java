package com.epam.trainingservice.service.jms;

import com.epam.trainingservice.controller.TrainingController;
import com.epam.trainingservice.dto.TrainingInfoRequest;
import com.epam.trainingservice.service.SummaryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;


@Service
public class TrainingInfoReceiver {
    private final TrainingController trainingController;
    private final SummaryService summaryService;
    private final ObjectMapper objectMapper;
    public static final Logger log = LoggerFactory.getLogger(TrainingInfoReceiver.class);

    public TrainingInfoReceiver(TrainingController trainingController, SummaryService summaryService, ObjectMapper objectMapper) {
        this.trainingController = trainingController;
        this.summaryService = summaryService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "training.info.queue")
    public void receive(Message message) throws JsonProcessingException {
        TrainingInfoRequest request = objectMapper.readValue(message.getPayload().toString(), TrainingInfoRequest.class);
        log.info(request.toString());
        trainingController.saveInfo(request);
        summaryService.processByUsername(request.getUsername());
    }
}
