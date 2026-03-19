package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.jobPosting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface jobPostingRepository extends MongoRepository<jobPosting, String> {
    List<jobPosting> findByTargetRoleIgnoreCase(String targetRole);
}
