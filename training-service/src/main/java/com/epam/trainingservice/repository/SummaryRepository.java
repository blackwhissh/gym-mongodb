//package com.epam.trainingservice.repository;
//
//import com.epam.trainingservice.entity.Summary;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//
//import java.util.Optional;
//
//public interface SummaryRepository extends MongoRepository<Summary, Long> {
//    Optional<Summary> findByTrainerUsername(String username);
//
//}
