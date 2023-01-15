package me.erikpelli.jdigital.noncompliance.noncompliances;

import me.erikpelli.jdigital.noncompliance.NonCompliance;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class NonCompliancesController {
    private final NonCompliancesService nonCompliancesService;

    public NonCompliancesController(NonCompliancesService nonCompliancesService) {
        this.nonCompliancesService = nonCompliancesService;
    }

    record GetDetailsInput(Integer resultsPerPage, Integer pageNumber, String search) {
    }

    /**
     * GET /api/noncompliances
     *
     * @param searchData resultsPerPage, pageNumber, [optional] search
     * @return list of nonComplianceCode
     */
    @GetMapping("/noncompliances")
    public List<Map<String, Integer>> getNonCompliancesListOrderedByMostRecent(
            @RequestBody(required = false)
            GetDetailsInput searchData) {
        if ((searchData.resultsPerPage() == null || searchData.pageNumber() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }

        var resultsPerPage = searchData.resultsPerPage();
        var pageNumber = searchData.pageNumber();
        var searchTerms = searchData.search();

        List<NonCompliance> nonComplianceList = nonCompliancesService.getNonCompliances(resultsPerPage, pageNumber, searchTerms);
        return nonComplianceList.stream().map(
                (NonCompliance nc) -> Map.of("nonComplianceCode", nc.getCode())
        ).toList();
    }

    /**
     * POST /api/noncompliances
     *
     * @return totalNonCompliances{new, progress, review, closed},
     * days - list of {date, new, progress, review, closed}
     */
    @Cacheable("nonCompliancesStatistics")
    @PostMapping("/noncompliances")
    public Map<String, Object> returnTotalNumberNonComplianceByStatusAndLast30DaysStats() {
        var totalStats = nonCompliancesService.getNonCompliancesTotalStats();
        var nonCompliancesPerDayList = nonCompliancesService.getLastMonthStats();
        return Map.of("totalNonCompliances", totalStats, "days", nonCompliancesPerDayList);
    }
}
