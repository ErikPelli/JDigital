package me.erikpelli.jdigital.ticket;

import org.springframework.data.repository.CrudRepository;

public interface TicketRepository extends CrudRepository<Ticket, TicketKey> {
}
