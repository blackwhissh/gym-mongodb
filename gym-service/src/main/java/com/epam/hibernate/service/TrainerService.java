package com.epam.hibernate.service;

import com.epam.hibernate.dto.trainee.TraineeListInfo;
import com.epam.hibernate.dto.trainer.request.TrainerRegisterRequest;
import com.epam.hibernate.dto.trainer.request.TrainerTrainingsRequest;
import com.epam.hibernate.dto.trainer.request.UpdateTrainerRequest;
import com.epam.hibernate.dto.trainer.response.TrainerProfileResponse;
import com.epam.hibernate.dto.trainer.response.TrainerRegisterResponse;
import com.epam.hibernate.dto.trainer.response.TrainerTrainingsResponse;
import com.epam.hibernate.dto.trainer.response.UpdateTrainerResponse;
import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.TrainingTypeRepository;
import com.epam.hibernate.repository.UserJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.hibernate.Utils.generateUsername;

@Service
public class TrainerService {
    private final Timer registerTimer;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerRepository trainerRepository;
    private final UserJpaRepository userRepository;

    @Autowired
    public TrainerService(MeterRegistry meterRegistry, TrainingTypeRepository trainingTypeRepository, TrainerRepository trainerRepository, UserJpaRepository userRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.registerTimer = Timer.builder("trainer_register_timer").description("Times Registering Trainer").register(meterRegistry);
    }

    public ResponseEntity<TrainerRegisterResponse> createProfile(@NotNull TrainerRegisterRequest request) {
        User trainerUser = new User(request.getFirstName(), request.getLastName(), true, RoleEnum.TRAINER);
        registerTimer.record(() -> {
            if (userRepository.existsByUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false))) {
                trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false));
            } else {
                trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), true));
            }
            Trainer trainer = new Trainer(trainingTypeRepository.selectByType(TrainingTypeEnum.valueOf(request.getSpecialization().toUpperCase())), trainerUser);
            trainerRepository.save(trainer);
        });
        return ResponseEntity.ok().body(new TrainerRegisterResponse(trainerUser.getUsername(), trainerUser.getTempPass()));
    }

    public ResponseEntity<TrainerProfileResponse> selectTrainerProfile(@NotNull String username) {
        Trainer trainer = trainerRepository.selectByUsername(username);
        return ResponseEntity.ok(new TrainerProfileResponse(trainer.getUser().getFirstName(), trainer.getUser().getLastName(), trainer.getSpecialization(), trainer.getUser().getActive(), trainer.getTrainees().stream().map(trainee -> new TraineeListInfo(trainee.getUser().getUsername(), trainee.getUser().getFirstName(), trainee.getUser().getLastName())).collect(Collectors.toSet())));
    }

    public ResponseEntity<UpdateTrainerResponse> updateTrainer(@NotNull String username, @NotNull UpdateTrainerRequest request) {
        Trainer trainer = trainerRepository.updateTrainer(username, request.getFirstName(), request.getLastName(), request.getActive(), request.getSpecialization());
        return ResponseEntity.ok().body(new UpdateTrainerResponse(trainer.getUser().getUsername(), trainer.getUser().getFirstName(), trainer.getUser().getLastName(), trainer.getSpecialization(), trainer.getUser().getActive(), trainer.getTrainees().stream().map(trainee -> new TraineeListInfo(trainee.getUser().getUsername(), trainee.getUser().getFirstName(), trainee.getUser().getLastName())).collect(Collectors.toSet())));
    }

    @Transactional
    public ResponseEntity<List<TrainerTrainingsResponse>> getTrainingList(@NotNull String username, @NotNull TrainerTrainingsRequest request) {
        List<Training> trainingList = trainerRepository.getTrainingList(username, request.getFrom(), request.getTo(), request.getTraineeName(), trainerRepository.selectByUsername(username).getSpecialization().getTrainingTypeName());
        return ResponseEntity.ok(trainingList.stream().map(training -> new TrainerTrainingsResponse(training.getTrainingName(), training.getTrainingDate(), training.getTrainingType(), training.getTrainingDuration(), training.getTrainer().getUser().getFirstName())).toList());
    }


}
