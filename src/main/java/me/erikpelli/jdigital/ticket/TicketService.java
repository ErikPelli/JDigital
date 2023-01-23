package me.erikpelli.jdigital.ticket;

import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.noncompliance.NonComplianceRepository;
import me.erikpelli.jdigital.shipping.ShippingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CompanyRepository companyRepository;
    private final ShippingRepository shippingRepository;
    private final NonComplianceRepository nonComplianceRepository;

    public TicketService(TicketRepository ticketRepository,
                         CompanyRepository companyRepository,
                         ShippingRepository shippingRepository,
                         NonComplianceRepository nonComplianceRepository) {
        this.ticketRepository = ticketRepository;
        this.companyRepository = companyRepository;
        this.shippingRepository = shippingRepository;
        this.nonComplianceRepository = nonComplianceRepository;
    }

    /**
     * Add a new ticket in the database.
     *
     * @param customerVat       VAT number of the customer company
     * @param shippingCode      shipping code identifier
     * @param nonComplianceCode non compliance identifier
     * @param description       Optional description that explain what the problem is
     */
    public void addTicket(String customerVat, String shippingCode, Integer nonComplianceCode, Optional<String> description) {
        if (customerVat == null || shippingCode == null || nonComplianceCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing mandatory parameters"
            );
        }

        var company = companyRepository.findFirstByVatNum(customerVat);
        var lot = shippingRepository.findByShippingCode(shippingCode);
        var nonCompliance = nonComplianceRepository.findByCode(nonComplianceCode);

        if (company == null || lot == null || nonCompliance == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "referenced entities not found"
            );
        }

        var descriptionText = description.orElse("");
        var newTicket = new Ticket(company, nonCompliance, lot, descriptionText, null);
        ticketRepository.save(newTicket);
    }

    /**
     * Get a ticket from DB using its identifier.
     * @param identifier unique identifier of the ticket
     * @return Ticket object
     */
    private Ticket getTicketObject(TicketIdentifier identifier) {
        var company = companyRepository.findFirstByVatNum(identifier.companyId());
        var nonCompliance = nonComplianceRepository.findByCode(identifier.nonComplianceId());
        var ticket = ticketRepository.findById(new TicketKey(company, nonCompliance));
        if (company == null || nonCompliance == null || ticket.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "ticket not found"
            );
        }
        return ticket.get();
    }

    /**
     * Answer to an existing ticket.
     *
     * @param ticketKey identifier of the ticket
     * @param answer    response to save
     */
    public void answerToTicket(TicketIdentifier ticketKey, String answer) {
        if (ticketKey == null || answer == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing mandatory parameters"
            );
        }

        var foundTicket = getTicketObject(ticketKey);
        foundTicket.setAnswer(answer);
        ticketRepository.save(foundTicket);
    }

    /**
     * Close a ticket.
     *
     * @param ticketKey identifier of the ticket
     */
    public void closeTicket(TicketIdentifier ticketKey) {
        if (ticketKey == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing ticket key"
            );
        }

        var foundTicket = getTicketObject(ticketKey);
        foundTicket.setClosed(true);
        ticketRepository.save(foundTicket);
    }

    /**
     * Get Information about a ticket.
     *
     * @param ticketKey identifier of the ticket
     * @return Ticket object
     */
    public Ticket getTicket(TicketIdentifier ticketKey) {
        if (ticketKey == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "missing ticket key"
            );
        }

        return getTicketObject(ticketKey);
    }
}
