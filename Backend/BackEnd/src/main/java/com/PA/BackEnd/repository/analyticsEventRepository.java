package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.analyticsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface analyticsEventRepository extends MongoRepository<analyticsEvent, String> {
    List<analyticsEvent> findByUserIdOrderByCreatedAtDesc(String userId);
}
