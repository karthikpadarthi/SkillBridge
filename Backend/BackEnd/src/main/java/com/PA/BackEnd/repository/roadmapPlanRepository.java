package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.roadmapPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface roadmapPlanRepository extends MongoRepository<roadmapPlan, String> {
    List<roadmapPlan> findByUserIdOrderByGeneratedAtDesc(String userId);
}
