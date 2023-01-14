package me.erikpelli.jdigital.noncompliance.type;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface NonComplianceTypeRepository extends CrudRepository<NonComplianceType, Integer> {
    @NonNull
    @Override
    List<NonComplianceType> findAll();
}
