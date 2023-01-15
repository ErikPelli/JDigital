package me.erikpelli.jdigital.noncompliance.type;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NonComplianceTypeController {
    private final NonComplianceTypeService nonComplianceTypeService;

    public NonComplianceTypeController(NonComplianceTypeService nonComplianceTypeService) {
        this.nonComplianceTypeService = nonComplianceTypeService;
    }

    /**
     * PUT /api/noncompliances
     *
     * @return list of code, name, description
     */
    @PutMapping("/noncompliances")
    public List<NonComplianceType> getAvailableNonComplianceTypes() {
        return nonComplianceTypeService.getPossibleNonCompliances();
    }
}
