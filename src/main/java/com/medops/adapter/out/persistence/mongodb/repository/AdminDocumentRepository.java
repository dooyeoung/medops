package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.AdminDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminDocumentRepository extends MongoRepository<AdminDocument, String> {
    @Query("{email: ?0}")
    Optional<AdminDocument> findByEmail(String email);

    List<AdminDocument> findAllByHospitalId(String hospitalId);

    boolean existsByEmail(String email);
}
