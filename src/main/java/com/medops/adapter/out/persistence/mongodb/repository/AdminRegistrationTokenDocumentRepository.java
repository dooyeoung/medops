package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.AdminRegistrationTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface AdminRegistrationTokenDocumentRepository extends MongoRepository<AdminRegistrationTokenDocument, String> {
//    @Query("{adminId: ?0}")
    Optional<AdminRegistrationTokenDocument> findByAdminId(String adminId);
}
