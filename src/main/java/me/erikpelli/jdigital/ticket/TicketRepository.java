package me.erikpelli.jdigital.ticket;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public interface TicketRepository extends CrudRepository<Ticket, TicketKey> {
    @NonNull
    List<Ticket> findAll(Pageable pageable);

    interface TicketsPerDay {
        Date getDate();

        Integer getCounter();
    }

    @NonNull
    @Query("""
            select t.nonCompliance.date as date, count(*) as counter
            from Ticket t
            where t.nonCompliance.date > :oneMonthAgoDate and t.nonCompliance.date <= :todayDate
            group by t.nonCompliance.date""")
    List<TicketsPerDay> last30DaysStats(@Param("oneMonthAgoDate") LocalDate oneMonthAgoDate,
                                        @Param("todayDate") LocalDate todayDate);
}
