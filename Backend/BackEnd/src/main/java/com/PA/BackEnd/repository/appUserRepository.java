package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.appUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface appUserRepository extends MongoRepository<appUser, String> {
    Optional<appUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
