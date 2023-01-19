package me.erikpelli.jdigital.ticket.tickets;

import me.erikpelli.jdigital.ticket.Ticket;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class TicketsController {
    private final TicketsService ticketsService;

    public TicketsController(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    /**
     * GET /api/tickets
     *
     * @param ticketsParam resultsPerPage, pageNumber
     * @return list of vatNum and nonComplianceCode
     */
    @GetMapping("/tickets")
    public List<Map<String, Object>> getTicketsInPage(
            @RequestBody(required = false)
            Map<String, Integer> ticketsParam) {
        if ((ticketsParam.get("resultsPerPage") == null || ticketsParam.get("pageNumber") == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }

        var ticketList = ticketsService.getTickets(ticketsParam.get("resultsPerPage"), ticketsParam.get("pageNumber"));
        return ticketList.stream().map((Ticket ticket) -> Map.<String, Object>of(
                "vatNum", ticket.getCustomer().getVatNum(),
                "nonComplianceCode", ticket.getNonCompliance().getCode()
        )).toList();
    }

    /**
     * POST /api/tickets
     *
     * @return totalTickets and days (list of date and counter)
     */
    @PostMapping("/tickets")
    @Cacheable("ticketsStatistics")
    public Map<String, Object> getTicketsStatistics() {
        var totalTickets = ticketsService.totalTickets();
        var lastMonthStats = ticketsService.getLastMonthStats(null);
        return Map.of("totalTickets", totalTickets, "days", lastMonthStats);
    }
}
