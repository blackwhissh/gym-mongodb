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

    public boolean saveInfo(TrainingInfoMessage request) {
        try {
            if (Arrays.stream(ActionType.values()).noneMatch(actionType ->
                    request.getActionType().equals(actionType))) {
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
                if (ActionType.ADD.equals(request.getActionType())) {
                    trainer.get().setTotalHours(trainer.get().getTotalHours() + request.getDuration());
                } else {
                    trainer.get().setTotalHours(trainer.get().getTotalHours() - request.getDuration());
                    Optional<Workload> workload = workloadRepository.findFirstByDayAndMonthAndYearAndTrainerAndTrainingDuration(
                            getDay(request.getTrainingDate()),
                            getMonth(request.getTrainingDate()),
                            getYear(request.getTrainingDate()),
                            trainer.get(),
                            request.getDuration());

                    Workload current = workload.orElseThrow(WorkloadNotFoundException::new);
                    request.setDuration(0);
                    current.setTrainingDuration(request.getDuration());
                    current.setActionType(ActionType.CANCEL);
                    workloadRepository.save(current);
                }

            }
            trainerRepository.save(trainer.get());
            if(request.getDuration() > 0){
                workloadRepository.save(new Workload(
                        getYear(request.getTrainingDate()),
                        getMonth(request.getTrainingDate()),
                        getDay(request.getTrainingDate()),
                        request.getDuration(),
                        request.getActionType(),
                        trainer.get()
                ));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
