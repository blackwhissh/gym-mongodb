package com.epam.trainingservice.service;

import com.epam.trainingservice.config.LogEntryExit;
import com.epam.trainingservice.dto.TrainerSummary;
import com.epam.trainingservice.entity.Summary;
import com.epam.trainingservice.repository.SummaryRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SummaryService {
    private final SummaryRepo summaryRepository;
    private final WorkloadService workloadService;

    public SummaryService(SummaryRepo summaryRepository, WorkloadService workloadService) {
        this.summaryRepository = summaryRepository;
        this.workloadService = workloadService;
    }
    @LogEntryExit(showArgs = true, showResult = true)
    public ResponseEntity<?> processByUsername(String username) {
        TrainerSummary trainerSummary = workloadService.getTrainerSummary(username).getBody();
        Summary summary = new Summary(
                Objects.requireNonNull(trainerSummary).getUsername(),
                trainerSummary.getFirstName(),
                trainerSummary.getLastName(),
                trainerSummary.isStatus(),
                trainerSummary.getYears());

        if(summaryRepository.findByUsername(username) == null){
            summaryRepository.save(summary);
            return ResponseEntity.ok("Summary has been saved");
        }else {
            summaryRepository.updateDuration(username,summary);
            return ResponseEntity.ok("Summary has been updated");
        }

    }
}
