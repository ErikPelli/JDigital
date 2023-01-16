package me.erikpelli.jdigital.noncompliance;

import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceTypeRepository;
import me.erikpelli.jdigital.shipping.ShippingRepository;
import me.erikpelli.jdigital.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NonComplianceService {
    private final NonComplianceRepository nonComplianceRepository;
    private final ShippingRepository shippingRepository;
    private final UserRepository userRepository;
    private final NonComplianceTypeRepository nonComplianceTypeRepository;

    public NonComplianceService(NonComplianceRepository nonComplianceRepository,
                                ShippingRepository shippingRepository,
                                UserRepository userRepository,
                                NonComplianceTypeRepository nonComplianceTypeRepository) {
        this.nonComplianceRepository = nonComplianceRepository;
        this.shippingRepository = shippingRepository;
        this.userRepository = userRepository;
        this.nonComplianceTypeRepository = nonComplianceTypeRepository;
    }

    /**
     * Get details about a non compliance.
     *
     * @param code unique identifier of noncompliance
     * @return Map with details already set
     */
    public Map<String, Object> getNonComplianceInfo(int code) {
        var nonCompliance = nonComplianceRepository.findById(code);
        if(nonCompliance.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "noncompliance not found");
        }
        var foundNonCompliance = nonCompliance.get();

        var mandatoryMap = Map.ofEntries(
                Map.entry("origin", foundNonCompliance.getOrigin()),
                Map.entry("nonComplianceType", foundNonCompliance.getType().getCode()),
                Map.entry("nonComplianceDate", foundNonCompliance.getFormattedDate()),
                Map.entry("shippingLot", foundNonCompliance.getLot().getShippingCode())
        );
        var withOptionalOutput = new HashMap<String, Object>(mandatoryMap);

        if (!foundNonCompliance.getComment().equals("")) {
            withOptionalOutput.put("comment", foundNonCompliance.getComment());
        }
        if (foundNonCompliance.getNonComplianceState().getResultDescription() != null) {
            withOptionalOutput.put("result", foundNonCompliance.getNonComplianceState().getResultDescription());
        }

        var manager = foundNonCompliance.getNonComplianceState().getManager();
        if (manager != null) {
            withOptionalOutput.put("managerEmail", manager.getEmail());
        }
        var analysisDate = foundNonCompliance.getNonComplianceState().getFormattedAnalysisDate();
        if (analysisDate != null) {
            withOptionalOutput.put("analysisEndDate", analysisDate);
        }
        var checkDate = foundNonCompliance.getNonComplianceState().getFormattedCheckDate();
        if (checkDate != null) {
            withOptionalOutput.put("checkEndDate", checkDate);
        }

        return withOptionalOutput;
    }

    /**
     * Increment this noncompliance status to the next level, with the necessary parameters.
     * @param ncCode identifier of the nonCompliance
     * @param nextStatus expected next noncompliance status
     * @param managerFiscalCode identifier of the manager that will handle the noncompliance
     * @param result description of the fix when the noncompliance is closed
     */
    public void nextStatus(int ncCode, NonComplianceStatus nextStatus, Optional<String> managerFiscalCode, Optional<String> result) {
        var nonCompliance = nonComplianceRepository.findById(ncCode);
        if(nonCompliance.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "noncompliance not found");
        }
        var foundNonCompliance = nonCompliance.get();
        if (foundNonCompliance.getNonComplianceState().getStatus().nextStatus() != nextStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mismatched expected next status");
        }
        var currentState = foundNonCompliance.getNonComplianceState();

        switch (nextStatus) {
            case ANALYSIS, CHECK -> {
                if (managerFiscalCode.isPresent()) {
                    if (managerFiscalCode.get().length() != 16) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid manager identifier");
                    }
                    var manager = userRepository.findById(managerFiscalCode.get());
                    if(manager.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "manager not found");
                    }
                    currentState.setManager(manager.get());
                }

                // Set 1 month expire date
                var expireDate = Date.valueOf(LocalDate.now().plusMonths(1));
                if (nextStatus == NonComplianceStatus.ANALYSIS) {
                    currentState.setAnalysisDate(expireDate);
                } else {
                    currentState.setCheckDate(expireDate);
                }
            }
            case RESULT -> currentState.setResultDescription(result.orElse(""));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unexpected next status");
        }

        currentState.setStatus(nextStatus);
        foundNonCompliance.setNonComplianceState(currentState);
        nonComplianceRepository.save(foundNonCompliance);
    }

    /**
     * Create a new noncompliance.
     *
     * @param origin origin of the noncompliance (internal/external)
     * @param lot production lot identifier
     * @param typeId noncompliance category identifier
     * @param comment additional information about the problem
     */
    public void createNewNonCompliance(NonComplianceOrigin origin, String lot, Integer typeId, String comment) {
        if (origin == null || lot == null || typeId == null || comment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mandatory parameters are missing");
        }
        var shippingLot = shippingRepository.findByShippingCode(lot);
        if(shippingLot == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "shipping lot not found");
        }
        var nonComplianceType = nonComplianceTypeRepository.findById(typeId);
        if(nonComplianceType.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "non compliance type not found");
        }
        NonCompliance newNonCompliance = new NonCompliance(shippingLot, nonComplianceType.get(), origin, comment);
        nonComplianceRepository.save(newNonCompliance);
    }

    /**
     * Create a new noncompliance with an empty comment.
     *
     * @param origin origin of the noncompliance (internal/external)
     * @param lot production lot identifier
     * @param typeId noncompliance category identifier
     */
    public void createNewNonCompliance(NonComplianceOrigin origin, String lot, Integer typeId) {
        createNewNonCompliance(origin, lot, typeId, "");
    }
}
