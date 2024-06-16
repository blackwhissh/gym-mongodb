package com.epam.trainingservice.config;

import com.epam.trainingservice.entity.Summary;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps(Summary.class);

        indexOps.ensureIndex(new Index().on("trainerFirstName", Sort.Direction.ASC)
                .on("trainerLastName", Sort.Direction.ASC)
                .named("firstName_lastName_index"));
    }
}
