package me.erikpelli.jdigital.noncompliance;

import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
public class NonComplianceController {
    private final NonComplianceService nonComplianceService;

    public NonComplianceController(NonComplianceService nonComplianceService) {
        this.nonComplianceService = nonComplianceService;
    }

    /**
     * GET /api/noncompliance
     *
     * @param nonComplianceData nonCompliance
     * @return origin, nonComplianceType, nonComplianceDate, shippingLot,
     * [optional] comment, managerEmail
     * In progress: analysisEndDate
     * In review: analysisEndDate, checkEndDate
     * Closed: analysisEndDate, checkEndDate, result
     */
    @GetMapping("/noncompliance")
    public Map<String, Object> getDetailsSingleNonCompliance(
            @RequestBody(required = false)
            Map<String, Integer> nonComplianceData) {
        var nonComplianceId = nonComplianceData == null ? null : nonComplianceData.get("nonCompliance");
        if (nonComplianceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid noncompliance id");
        }
        return nonComplianceService.getNonComplianceInfo(nonComplianceId);
    }

    record IncrementNonComplianceInput(Integer nonCompliance, NonComplianceStatus status, String manager,
                                       String resultComment) {
    }

    /**
     * POST /api/noncompliance
     *
     * @param nonComplianceData nonCompliance, status, [optional] manager, [optional] resultComment
     */
    @CacheEvict(value = "nonCompliancesStatistics", allEntries = true)
    @PostMapping("/noncompliance")
    public Object incrementNonComplianceStatus(
            @RequestBody(required = false)
            IncrementNonComplianceInput nonComplianceData) {
        if (nonComplianceData == null || nonComplianceData.nonCompliance() == null || nonComplianceData.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing mandatory parameters");
        }
        nonComplianceService.nextStatus(
                nonComplianceData.nonCompliance(),
                nonComplianceData.status(),
                Optional.ofNullable(nonComplianceData.manager()),
                Optional.ofNullable(nonComplianceData.resultComment())
        );
        return Map.of();
    }

    record NewNonComplianceInput(NonComplianceOrigin nonComplianceOrigin, Integer nonComplianceType,
                                 String shippingLot, String comment) {
    }

    /**
     * PUT /api/noncompliance
     *
     * @param nc nonComplianceOrigin, nonComplianceType, shippingLot, [optional] comment
     */
    @CacheEvict(value = "nonCompliancesStatistics", allEntries = true)
    @PutMapping("/noncompliance")
    public Object createNonCompliance(
            @RequestBody(required = false)
            NewNonComplianceInput nc) {
        if (nc == null || nc.nonComplianceOrigin() == null || nc.nonComplianceType() == null || nc.shippingLot() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid noncompliance input");
        }
        if (nc.comment() == null) {
            nonComplianceService.createNewNonCompliance(nc.nonComplianceOrigin(), nc.shippingLot(), nc.nonComplianceType());
        } else {
            nonComplianceService.createNewNonCompliance(nc.nonComplianceOrigin(), nc.shippingLot(), nc.nonComplianceType(), nc.comment());
        }
        return Map.of();
    }
}
