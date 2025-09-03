package com.medops.adapter.out.persistence.mongodb.repository;

import com.medops.adapter.out.persistence.mongodb.document.BusinessHourDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface BusinessHourDocumentRepository extends MongoRepository<BusinessHourDocument, String> {
    @Query("{hospitalId: ?0}")
    List<BusinessHourDocument> findAllByHospitalId(String hospitalId);
}
