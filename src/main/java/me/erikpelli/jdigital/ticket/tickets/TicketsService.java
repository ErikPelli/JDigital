package me.erikpelli.jdigital.ticket.tickets;

import me.erikpelli.jdigital.ticket.Ticket;
import me.erikpelli.jdigital.ticket.TicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketsService {
    private final TicketRepository ticketRepository;

    public TicketsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Get a list of tickets using pagination.
     *
     * @param resultsPerPage number of entries for every page
     * @param pageNumber     number of the page to retrieve
     * @return List of tickets
     */
    public List<Ticket> getTickets(int resultsPerPage, int pageNumber) {
        if (resultsPerPage > 0 && pageNumber > 0) {
            var pagination = PageRequest.of(pageNumber, resultsPerPage, Sort.by("nonCompliance.date", "nonCompliance.code").descending());
            return ticketRepository.findAll(pagination);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }
    }

    /**
     * Return the number of all tickets from the start.
     *
     * @return number of tickets
     */
    public long totalTickets() {
        return ticketRepository.count();
    }

    /**
     * Get statistics about the tickets in the last 30 days.
     *
     * @return list of TicketsPerDay entries
     */
    public List<Map<String, Object>> getLastMonthStats(LocalDate testLocalDate) {
        LocalDate today = testLocalDate != null ? testLocalDate : LocalDate.now();
        // 30th day is today, so we start from 29 days ago
        LocalDate oneMonthAgo = today.minusDays(29);
        var last30DaysStats = ticketRepository.last30DaysStats(oneMonthAgo, today);
        var statsMap = last30DaysStats.stream().collect(Collectors.toMap(TicketRepository.TicketsPerDay::getDate, TicketRepository.TicketsPerDay::getCounter));

        // Add zero values in result map where database doesn't provide data
        while (!oneMonthAgo.isAfter(today)) {
            statsMap.putIfAbsent(Date.valueOf(oneMonthAgo.toString()), 0);
            oneMonthAgo = oneMonthAgo.plusDays(1);
        }

        return statsMap.entrySet().stream()
                // Transform data received and set counter to 0 where there is no data
                .map(dateMapEntry -> Map.<String, Object>of(
                        "date", dateMapEntry.getKey().toString(),
                        "counter", dateMapEntry.getValue()
                ))
                // Return a list sorted by date (most recent first)
                .sorted(Comparator.comparing((Map<String, Object> obj) -> ((String) obj.get("date"))).reversed())
                .toList();
    }
}
