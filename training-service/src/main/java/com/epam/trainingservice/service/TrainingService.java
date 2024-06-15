package com.epam.trainingservice.service;

import com.epam.trainingservice.dto.TrainingInfoRequest;
import com.epam.trainingservice.entity.Trainer;
import com.epam.trainingservice.entity.Workload;
import com.epam.trainingservice.entity.enums.ActionType;
import com.epam.trainingservice.repository.TrainerRepository;
import com.epam.trainingservice.repository.WorkloadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

import static com.epam.trainingservice.utils.DateUtils.*;

@Service
public class TrainingService {
    private final TrainerRepository trainerRepository;
    private final WorkloadRepository workloadRepository;

    public TrainingService(TrainerRepository trainerRepository, WorkloadRepository workloadRepository) {
        this.trainerRepository = trainerRepository;
        this.workloadRepository = workloadRepository;
    }

    public ResponseEntity<HttpStatus> saveInfo(TrainingInfoRequest request) {
        try {
            if (Arrays.stream(ActionType.values()).noneMatch(actionType ->
                    request.getActionType().toUpperCase().equals(actionType.getValue()))) {
                throw new IllegalArgumentException("Wrong Action Type");
            }

            Optional<Trainer> trainer = trainerRepository.findByUsername(request.getUsername());
            if (trainer.isEmpty()) {
                trainer = Optional.of(new Trainer(
                        request.getUsername(),
                        request.getFirstName(),
                        request.getLastName(),
                        request.getActive(),
                        request.getDuration()));
                trainerRepository.save(trainer.get());

            } else {
                if (request.getActionType().equals("ADD")){
                    trainer.get().setTotalHours(trainer.get().getTotalHours() + request.getDuration());
                }else{
                    trainer.get().setTotalHours(trainer.get().getTotalHours() - request.getDuration());
                }

            }

            workloadRepository.save(new Workload(
                    getYear(request.getTrainingDate()),
                    getMonth(request.getTrainingDate()),
                    getDay(request.getTrainingDate()),
                    request.getDuration(),
                    ActionType.valueOf(request.getActionType()),
                    trainer.get()
            ));

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
