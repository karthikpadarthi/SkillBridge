package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.jobRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface jobRepository extends MongoRepository<jobRole, String> {
    Optional<jobRole> findByRoleNameIgnoreCase(String roleName);
}
