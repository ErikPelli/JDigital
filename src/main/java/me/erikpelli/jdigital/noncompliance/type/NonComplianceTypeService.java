package me.erikpelli.jdigital.noncompliance.type;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NonComplianceTypeService {
    private final NonComplianceTypeRepository nonComplianceTypeRepository;

    public NonComplianceTypeService(NonComplianceTypeRepository nonComplianceTypeRepository) {
        this.nonComplianceTypeRepository = nonComplianceTypeRepository;
    }

    /**
     * Return all types of noncompliances.
     *
     * @return list of NonComplianceType
     */
    public List<NonComplianceType> getPossibleNonCompliances() {
        return nonComplianceTypeRepository.findAll();
    }
}
