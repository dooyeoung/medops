package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {

    @Query(value = "{email: ?0}")
    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
