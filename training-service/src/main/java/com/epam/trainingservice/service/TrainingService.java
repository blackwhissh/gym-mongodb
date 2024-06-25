package com.epam.trainingservice.service;

import com.epam.trainingservice.dto.TrainingInfoMessage;
import com.epam.trainingservice.entity.Trainer;
import com.epam.trainingservice.entity.Workload;
import com.epam.trainingservice.entity.enums.ActionType;
import com.epam.trainingservice.exception.WorkloadNotFoundException;
import com.epam.trainingservice.repository.TrainerRepository;
import com.epam.trainingservice.repository.WorkloadRepository;
import com.epam.trainingservice.utils.DateUtils;
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

    public boolean updateWorkload(TrainingInfoMessage request) {
        try {
            if (Arrays.stream(ActionType.values()).noneMatch(actionType ->
                    request.getActionType().equals(actionType))) {
                throw new IllegalArgumentException("Wrong Action Type");
            }

            Trainer trainer = findAndSaveTrainer(request);

            trainerRepository.save(trainer);
            if(request.getDuration() > 0){
                workloadRepository.save(new Workload(
                        getYear(request.getTrainingDate()),
                        getMonth(request.getTrainingDate()),
                        getDay(request.getTrainingDate()),
                        request.getDuration(),
                        request.getActionType(),
                        trainer
                ));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Trainer findAndSaveTrainer(TrainingInfoMessage message){
        Optional<Trainer> trainer = trainerRepository.findByUsername(message.getUsername());
        if (trainer.isEmpty()) {
            trainer = Optional.of(new Trainer(
                    message.getUsername(),
                    message.getFirstName(),
                    message.getLastName(),
                    message.getActive(),
                    message.getDuration()));
            trainerRepository.save(trainer.get());

        } else {
            if (ActionType.ADD.equals(message.getActionType())) {
                trainer.get().setTotalHours(trainer.get().getTotalHours() + message.getDuration());
            } else {
                trainer.get().setTotalHours(trainer.get().getTotalHours() - message.getDuration());
                Optional<Workload> workload = workloadRepository.findFirstByDayAndMonthAndYearAndTrainerAndTrainingDuration(
                        getDay(message.getTrainingDate()),
                        getMonth(message.getTrainingDate()),
                        getYear(message.getTrainingDate()),
                        trainer.get(),
                        message.getDuration());

                Workload current = workload.orElseThrow(WorkloadNotFoundException::new);
                message.setDuration(0);
                current.setTrainingDuration(message.getDuration());
                current.setActionType(ActionType.CANCEL);
                workloadRepository.save(current);
            }
        }
        return trainer.get();
    }
}
