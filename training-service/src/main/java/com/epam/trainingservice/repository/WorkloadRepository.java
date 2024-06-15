package com.epam.trainingservice.repository;

import com.epam.trainingservice.entity.Trainer;
import com.epam.trainingservice.entity.Workload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface WorkloadRepository extends JpaRepository<Workload, Long> {
    List<Workload> findByTrainerOrderByYearAsc(Trainer trainer);
    void deleteByYearAndMonthAndDayAndTrainer(int year, int month, int day, Trainer trainer);
}
