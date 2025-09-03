package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.HospitalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface HospitalDocumentRepository extends MongoRepository<HospitalDocument, String> {

    @Query(value = "{name: ?0}")
    Optional<HospitalDocument> findByName(String name);

    boolean existsByName(String name);
}
