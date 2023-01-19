package me.erikpelli.jdigital.ticket.tickets;

import me.erikpelli.jdigital.ticket.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketsServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    private TicketsService ticketsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketsService = new TicketsService(ticketRepository);
    }

    @Test
    void getTickets() {
        Mockito.when(ticketRepository.findAll(Mockito.any(Pageable.class))).thenReturn(List.of());

        assertThrows(Exception.class, () -> ticketsService.getTickets(1, 0));
        assertThrows(Exception.class, () -> ticketsService.getTickets(0, 1));

        var allTickets = ticketsService.getTickets(1, 1);
        assertEquals(0, allTickets.size());
    }

    @Test
    void totalTickets() {
        Mockito.when(ticketRepository.count()).thenReturn(Long.valueOf(76));
        var result = ticketsService.totalTickets();
        assertEquals(76, result);
    }

    @Test
    void getLastMonthStats() {
        final class TestEntry implements TicketRepository.TicketsPerDay {
            private final Date date;
            private final Integer counter;

            TestEntry(Date date, Integer counter) {
                this.date = date;
                this.counter = counter;
            }

            @Override
            public Date getDate() {
                return date;
            }

            @Override
            public Integer getCounter() {
                return counter;
            }
        }

        Mockito.when(ticketRepository.last30DaysStats(Mockito.any(), Mockito.any())).thenReturn(List.of(
                new TestEntry(Date.valueOf("2022-10-15"), 5),
                new TestEntry(Date.valueOf("2022-10-13"), 14),
                new TestEntry(Date.valueOf("2022-10-12"), 1)
        ));

        var todayDate = LocalDate.parse("2022-10-16");
        var lastMonthResult = ticketsService.getLastMonthStats(todayDate);
        assertEquals(30, lastMonthResult.size());

        // 2022-10-16
        assertEquals(lastMonthResult.get(0).get("date"), "2022-10-16");
        assertEquals(0, lastMonthResult.get(0).get("counter"));
        // 2022-10-15
        assertEquals(lastMonthResult.get(1).get("date"), "2022-10-15");
        assertEquals(5, lastMonthResult.get(1).get("counter"));
        // 2022-10-14
        assertEquals(lastMonthResult.get(2).get("date"), "2022-10-14");
        assertEquals(0, lastMonthResult.get(2).get("counter"));
        // 2022-10-13
        assertEquals(lastMonthResult.get(3).get("date"), "2022-10-13");
        assertEquals(14, lastMonthResult.get(3).get("counter"));
        // 2022-10-12
        assertEquals(lastMonthResult.get(4).get("date"), "2022-10-12");
        assertEquals(1, lastMonthResult.get(4).get("counter"));

        // Check that all other days are empty
        for (var i = 5; i < lastMonthResult.size(); i++) {
            assertEquals(todayDate.minusDays(i).toString(), lastMonthResult.get(i).get("date"));
            assertEquals(0, lastMonthResult.get(i).get("counter"));
        }
    }
}