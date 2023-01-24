package me.erikpelli.jdigital.ticket;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    record TicketInput(String vat, Integer nonCompliance, String answer, String shippingLot, String description) {
    }

    /**
     * GET /api/ticket
     *
     * @param ticketData vat, nonCompliance
     * @return customerCompanyName, customerCompanyAddress, shippingCode, productQuantity, problemDescription,
     * [new, progress, closed] status, [optional] ticketAnswer
     */
    @GetMapping("/ticket")
    public Map<String, Object> getDetailsAboutSingleTicket(
            @RequestBody(required = false)
            TicketInput ticketData) {
        if (ticketData.vat() == null || ticketData.nonCompliance() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing mandatory parameters"
            );
        }
        var ticketKey = new TicketIdentifier(ticketData.vat(), ticketData.nonCompliance());
        var ticketFound = ticketService.getTicket(ticketKey);
        var result = new HashMap<String, Object>(Map.ofEntries(
                Map.entry("customerCompanyName", ticketFound.getCustomer().getName()),
                Map.entry("customerCompanyAddress", ticketFound.getCustomer().getAddress()),
                Map.entry("shippingCode", ticketFound.getLot().getShippingCode()),
                Map.entry("productQuantity", ticketFound.getLot().getProductQuantity()),
                Map.entry("problemDescription", ticketFound.getDescription()),
                Map.entry("status", ticketFound.calculateStatus())
        ));
        if (ticketFound.getAnswer() != null) {
            result.put("ticketAnswer", ticketFound.getAnswer());
        }
        return result;
    }

    /**
     * POST /api/ticket
     *
     * @param ticketData vat, nonCompliance, answer
     */
    @PostMapping("/ticket")
    @CacheEvict(value="ticketsStatistics", allEntries=true)
    public Object setAnswerToTicket(
            @RequestBody(required = false)
            TicketInput ticketData) {
        if (ticketData.vat() == null || ticketData.nonCompliance() == null || ticketData.answer() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing mandatory parameters"
            );
        }
        var ticketKey = new TicketIdentifier(ticketData.vat(), ticketData.nonCompliance());
        ticketService.answerToTicket(ticketKey, ticketData.answer());
        return Map.of();
    }

    /**
     * PUT /api/ticket
     *
     * @param ticketData vat, nonCompliance, shippingLot, [optional] description
     */
    @PutMapping("/ticket")
    @CacheEvict(value="ticketsStatistics", allEntries=true)
    public Object createNewTicket(
            @RequestBody(required = false)
            TicketInput ticketData) {
        ticketService.addTicket(
                ticketData.vat(),
                ticketData.shippingLot(),
                ticketData.nonCompliance(),
                Optional.ofNullable(ticketData.description())
        );
        return Map.of();
    }

    /**
     * DELETE /api/ticket
     *
     * @param ticketData vat, nonCompliance
     */
    @DeleteMapping("/ticket")
    @CacheEvict(value="ticketsStatistics", allEntries=true)
    public Object closeSingleTicket(
            @RequestBody(required = false)
            TicketInput ticketData) {
        if (ticketData.vat() == null || ticketData.nonCompliance() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing mandatory parameters"
            );
        }
        var ticketKey = new TicketIdentifier(ticketData.vat(), ticketData.nonCompliance());
        ticketService.closeTicket(ticketKey);
        return Map.of();
    }
}
