package me.erikpelli.jdigital.noncompliance.noncompliances;

import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.noncompliance.NonComplianceOrigin;
import me.erikpelli.jdigital.noncompliance.NonComplianceRepository;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class NonCompliancesService {
    private final NonComplianceRepository nonComplianceRepository;

    public NonCompliancesService(NonComplianceRepository nonComplianceRepository) {
        this.nonComplianceRepository = nonComplianceRepository;
    }

    /**
     * Get a page of non compliances.
     *
     * @param resultsPerPage number of entries for every page
     * @param pageNumber     number of the page to retrieve
     * @param search         if not null, search for specific terms in query
     * @return list of noncompliances
     */
    public List<NonCompliance> getNonCompliances(int resultsPerPage, int pageNumber, String search) {
        if (!(resultsPerPage > 0 && pageNumber > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }

        var pagination = PageRequest.of(pageNumber - 1, resultsPerPage, Sort.by("date", "code").descending());
        if (search == null  || search.equals("")) {
            return nonComplianceRepository.findAll(pagination);
        }

        // Check if the search is a noncompliance id
        if (Pattern.matches("^[ICS]-[0-9]+", search)) {
            var origin = switch (search.charAt(0)) {
                case 'I' -> NonComplianceOrigin.INTERNAL;
                case 'C' -> NonComplianceOrigin.CUSTOMER;
                case 'S' -> NonComplianceOrigin.SUPPLIER;
                default -> null;
            };
            var id = Integer.valueOf(search.substring(2));
            return nonComplianceRepository.findByOriginAndCode(origin, id, pagination);
        }

        return nonComplianceRepository.findByCommentContaining(search, pagination);
    }

    /**
     * Get total number of noncompliances, divided by status.
     *
     * @return association between status and counter
     */
    public Map<NonComplianceStatus, Integer> getNonCompliancesTotalStats() {
        var mapWithDefaultValues = new HashMap<>(Map.of(
                NonComplianceStatus.NEW, 0,
                NonComplianceStatus.CHECK, 0,
                NonComplianceStatus.ANALYSIS, 0,
                NonComplianceStatus.RESULT, 0
        ));
        var totalCounters = nonComplianceRepository.totalStatsByStatus();

        // Overwrite default values where the counter is present in totalStatsByStatus() result
        mapWithDefaultValues.putAll(totalCounters);
        return mapWithDefaultValues;
    }

    /**
     * Get statistics about the noncompliances in the last 30 days.
     *
     * @return list of multiple days entries
     */
    public List<Map<Object, Object>> getLastMonthStats(LocalDate testLocalDate) {
        LocalDate today = testLocalDate != null ? testLocalDate : LocalDate.now();
        // 30th day is today, so we start from 29 days ago
        LocalDate oneMonthAgo = today.minusDays(29);
        var last30DaysStats = new HashMap<>(nonComplianceRepository.last30DaysStats(oneMonthAgo, today));

        // Add zero values in result map where database doesn't provide data
        while (!oneMonthAgo.isAfter(today)) {
            last30DaysStats.putIfAbsent(Date.valueOf(oneMonthAgo.toString()), Map.of());
            oneMonthAgo = oneMonthAgo.plusDays(1);
        }

        return last30DaysStats.entrySet().stream()
                // Transform data received and set counter to 0 where there is no data
                .map(dateMapEntry -> Map.<Object, Object>of(
                        "date", dateMapEntry.getKey().toString(),
                        NonComplianceStatus.NEW, Optional.ofNullable(dateMapEntry.getValue().get(NonComplianceStatus.NEW)).orElse(0),
                        NonComplianceStatus.ANALYSIS, Optional.ofNullable(dateMapEntry.getValue().get(NonComplianceStatus.ANALYSIS)).orElse(0),
                        NonComplianceStatus.CHECK, Optional.ofNullable(dateMapEntry.getValue().get(NonComplianceStatus.CHECK)).orElse(0),
                        NonComplianceStatus.RESULT, Optional.ofNullable(dateMapEntry.getValue().get(NonComplianceStatus.RESULT)).orElse(0)
                ))
                // Return a list sorted by date (most recent first)
                .sorted(Comparator.comparing((Map<Object, Object> obj) -> ((String) obj.get("date"))).reversed())
                .toList();
    }
}
