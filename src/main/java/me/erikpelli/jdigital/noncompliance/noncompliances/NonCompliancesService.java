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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

        var pagination = PageRequest.of(pageNumber, resultsPerPage, Sort.by("date", "code").descending());
        if (search == null) {
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
        var last30DaysStats = nonComplianceRepository.last30DaysStats(oneMonthAgo, today);

        // Fill a set with each day of the last month
        var emptyGeneration = new HashSet<String>(30);
        while (!oneMonthAgo.isAfter(today)) {
            var formattedDate = oneMonthAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            emptyGeneration.add(formattedDate);
            oneMonthAgo = oneMonthAgo.plusDays(1);
        }

        // Transform the data received from the database
        // Remove the days that contains a value from the set of empty days
        var databaseStream = last30DaysStats.entrySet().stream().map(dateMapEntry -> {
            var formattedDate = dateMapEntry.getKey().toString();
            emptyGeneration.remove(formattedDate);
            var statusCounters = dateMapEntry.getValue();
            return Map.<Object, Object>of(
                    "date", formattedDate,
                    NonComplianceStatus.NEW, Optional.ofNullable(statusCounters.get(NonComplianceStatus.NEW)).orElse(0),
                    NonComplianceStatus.ANALYSIS, Optional.ofNullable(statusCounters.get(NonComplianceStatus.ANALYSIS)).orElse(0),
                    NonComplianceStatus.CHECK, Optional.ofNullable(statusCounters.get(NonComplianceStatus.CHECK)).orElse(0),
                    NonComplianceStatus.RESULT, Optional.ofNullable(statusCounters.get(NonComplianceStatus.RESULT)).orElse(0)
            );
        });

        // Create a stream with zero values for days that weren't present in the DB result
        var missingEntriesStream = emptyGeneration.stream().map((String formattedDate) -> Map.<Object, Object>of(
                "date", formattedDate,
                NonComplianceStatus.NEW, 0,
                NonComplianceStatus.ANALYSIS, 0,
                NonComplianceStatus.CHECK, 0,
                NonComplianceStatus.RESULT, 0
        ));

        // Merge the two streams and sort by date with most recent first
        return Stream.of(databaseStream, missingEntriesStream)
                .flatMap(Function.identity())
                .sorted(Comparator.comparing((Map<Object, Object> obj) -> ((String) obj.get("date"))).reversed())
                .toList();
    }
}
