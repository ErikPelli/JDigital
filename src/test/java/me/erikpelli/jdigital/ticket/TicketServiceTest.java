package me.erikpelli.jdigital.ticket;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.noncompliance.NonComplianceRepository;
import me.erikpelli.jdigital.shipping.ShippingLot;
import me.erikpelli.jdigital.shipping.ShippingRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private NonComplianceRepository nonComplianceRepository;

    private TicketService ticketService;

    @BeforeAll
    void setUpImmutable() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketService(ticketRepository, companyRepository, shippingRepository, nonComplianceRepository);

        Mockito.when(shippingRepository.findByShippingCode(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String shippingCode = invocationOnMock.getArgument(0);
                    if (!shippingCode.equals("shipping1")) {
                        return null;
                    }
                    return new ShippingLot("shipping1", null);
                });
        Mockito.when(companyRepository.findFirstByVatNum(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String vat = invocationOnMock.getArgument(0);
                    if (!vat.equals("VAT1")) {
                        return null;
                    }
                    return new Company("VAT1", "default", "square 123");
                });
        Mockito.when(nonComplianceRepository.findByCode(Mockito.anyInt()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Integer ncCode = invocationOnMock.getArgument(0);
                    if (ncCode != 10) {
                        return null;
                    }
                    var nc = new NonCompliance(null, null, null, Date.valueOf("2020-09-17"), "");
                    nc.setCode(10);
                    return nc;
                });
    }

    @BeforeEach
    void setUp() {
        final var ticket = new Ticket(
                new Company("VAT1", "default", ""),
                new NonCompliance(null, null, null, null, null),
                null
        );
        ticket.getNonCompliance().setCode(10);
        Mockito.when(ticketRepository.findById(Mockito.any(TicketKey.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    TicketKey key = invocationOnMock.getArgument(0);
                    if(!(key.getCustomer().getVatNum().equals("VAT1")) || key.getNonCompliance().getCode() != 10) {
                        return Optional.empty();
                    }
                    return Optional.of(ticket);
                });
        Mockito.when(ticketRepository.save(Mockito.any(Ticket.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Ticket t = invocationOnMock.getArgument(0);
                    ticket.setNonCompliance(t.getNonCompliance());
                    ticket.setLot(t.getLot());
                    ticket.setCustomer(t.getCustomer());
                    ticket.setAnswer(t.getAnswer());
                    ticket.setDescription(t.getDescription());
                    ticket.setClosed(t.isClosed());
                    return ticket;
                });
    }

    @Test
    void getTicket() {
        assertThrows(Exception.class, () -> ticketService.getTicket(null));
        assertThrows(Exception.class, () -> ticketService.getTicket(new TicketIdentifier("VAT1", 9)));
        assertThrows(Exception.class, () -> ticketService.getTicket(new TicketIdentifier("VAT2", 10)));

        var ticket = assertDoesNotThrow(() -> ticketService.getTicket(new TicketIdentifier("VAT1", 10)));
        assertEquals("VAT1", ticket.getCustomer().getVatNum());
        assertEquals(10, ticket.getNonCompliance().getCode());
    }

    @Test
    void addTicket() {
        var id = new TicketIdentifier("VAT1", 10);
        assertThrows(Exception.class, () -> ticketService.addTicket(null, null, null, Optional.empty()));
        assertThrows(Exception.class, () -> ticketService.addTicket("VAT1", null, null, Optional.empty()));
        assertThrows(Exception.class, () -> ticketService.addTicket("VAT1", "shipping1", null, Optional.empty()));
        assertThrows(Exception.class, () -> ticketService.addTicket("VAT1", "shipping1", 9, Optional.empty()));

        assertDoesNotThrow(() -> ticketService.addTicket("VAT1", "shipping1", 10, Optional.empty()));
        assertEquals("", ticketService.getTicket(id).getDescription());
        assertEquals(TicketStatus.NEW, ticketService.getTicket(id).calculateStatus());

        assertDoesNotThrow(() -> ticketService.addTicket("VAT1", "shipping1", 10, Optional.of("1234")));
        assertEquals("1234", ticketService.getTicket(id).getDescription());
        assertEquals(TicketStatus.NEW, ticketService.getTicket(id).calculateStatus());
    }

    @Test
    void answerToTicket() {
        var id = new TicketIdentifier("VAT1", 10);
        assertNull(ticketService.getTicket(id).getAnswer());

        ticketService.answerToTicket(id, "test1234");
        assertEquals("test1234", ticketService.getTicket(id).getAnswer());
    }

    @Test
    void closeTicket() {
        var id = new TicketIdentifier("VAT1", 10);
        assertFalse(ticketService.getTicket(id).isClosed());

        ticketService.closeTicket(id);
        assertTrue(ticketService.getTicket(id).isClosed());
    }
}