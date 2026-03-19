package com.PA.BackEnd.repository;

import com.PA.BackEnd.model.interviewQuestionSet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface interviewQuestionSetRepository extends MongoRepository<interviewQuestionSet, String> {
    List<interviewQuestionSet> findByUserIdOrderByGeneratedAtDesc(String userId);
}
