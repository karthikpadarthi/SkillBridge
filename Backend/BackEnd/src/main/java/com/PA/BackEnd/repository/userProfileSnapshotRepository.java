package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.userProfileSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface userProfileSnapshotRepository extends MongoRepository<userProfileSnapshot, String> {
    List<userProfileSnapshot> findByUserIdOrderByCreatedAtDesc(String userId);
}
