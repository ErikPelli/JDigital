package me.erikpelli.jdigital.ticket;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.noncompliance.NonComplianceOrigin;
import me.erikpelli.jdigital.noncompliance.NonComplianceRepository;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceType;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceTypeRepository;
import me.erikpelli.jdigital.shipping.ShippingLot;
import me.erikpelli.jdigital.shipping.ShippingRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketRepositoryTest {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NonComplianceRepository nonComplianceRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private NonComplianceTypeRepository nonComplianceTypeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private ShippingLot shippingLot;

    private NonComplianceType nonComplianceType;

    private Company company;

    @BeforeAll
    void setUp() {
        shippingRepository.save(new ShippingLot("shipping1", null, Date.valueOf("2020-10-01")));
        nonComplianceType = nonComplianceTypeRepository.save(new NonComplianceType("broken part", null));
        shippingLot = shippingRepository.findByShippingCode("shipping1");
        company = companyRepository.save(new Company("12345678901", "", ""));
        assertNotNull(nonComplianceType);
        assertNotNull(shippingLot);
        assertNotNull(company);
    }

    @AfterAll
    void cleanUp() {
        nonComplianceRepository.deleteAll();
        shippingRepository.deleteAll();
        nonComplianceTypeRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void last30DaysStats() {
        var nonCompliances = nonComplianceRepository.saveAll(List.of(
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-01"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-02"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-02"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-04"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-04"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-05"), null)
        ));
        var iter = nonCompliances.iterator();

        // 0: NEW
        // 1: NEW
        // 2: PROGRESS
        // 3: PROGRESS
        // 4: PROGRESS
        // 5: RESULT
        var tickets = List.of(
                new Ticket(company, iter.next(), shippingLot),
                new Ticket(company, iter.next(), shippingLot),
                new Ticket(company, iter.next(), shippingLot),
                new Ticket(company, iter.next(), shippingLot),
                new Ticket(company, iter.next(), shippingLot),
                new Ticket(company, iter.next(), shippingLot)
        );

        tickets.get(2).setAnswer("");
        tickets.get(3).setAnswer("");
        tickets.get(4).setAnswer("");
        tickets.get(5).setClosed(true);

        ticketRepository.saveAll(tickets);

        var todayDate = LocalDate.parse("2003-10-05");

        var todayStats = ticketRepository.last30DaysStats(todayDate.minusDays(1), todayDate);
        assertEquals(1, todayStats.size());
        // 2003-10-05
        assertEquals(Date.valueOf("2003-10-05"), todayStats.get(0).getDate());
        assertEquals(1, todayStats.get(0).getCounter());

        var last3Days = ticketRepository.last30DaysStats(todayDate.minusDays(3), todayDate);
        assertEquals(2, last3Days.size());
        var last3DaysMap = Map.of(
                last3Days.get(0).getDate(), last3Days.get(0),
                last3Days.get(1).getDate(), last3Days.get(1)
        );
        // 2003-10-05
        assertNotNull(last3DaysMap.get(Date.valueOf("2003-10-05")));
        assertEquals(1, last3DaysMap.get(Date.valueOf("2003-10-05")).getCounter());
        // 2003-10-04
        assertNotNull(last3DaysMap.get(Date.valueOf("2003-10-04")));
        assertEquals(2, last3DaysMap.get(Date.valueOf("2003-10-04")).getCounter());
        // 2003-10-03
        assertNull(last3DaysMap.get(Date.valueOf("2003-10-03")));

        var last30Days = ticketRepository.last30DaysStats(todayDate.minusDays(30), todayDate);
        assertEquals(4, last30Days.size());
        var last30DaysMap = Map.of(
                last30Days.get(0).getDate(), last30Days.get(0),
                last30Days.get(1).getDate(), last30Days.get(1),
                last30Days.get(2).getDate(), last30Days.get(2),
                last30Days.get(3).getDate(), last30Days.get(3)
        );
        // 2003-10-05
        assertNotNull(last30DaysMap.get(Date.valueOf("2003-10-05")));
        assertEquals(1, last30DaysMap.get(Date.valueOf("2003-10-05")).getCounter());
        // 2003-10-04
        assertNotNull(last30DaysMap.get(Date.valueOf("2003-10-04")));
        assertEquals(2, last30DaysMap.get(Date.valueOf("2003-10-04")).getCounter());
        // 2003-10-03
        assertNull(last30DaysMap.get(Date.valueOf("2003-10-03")));
        // 2003-10-02
        assertNotNull(last30DaysMap.get(Date.valueOf("2003-10-02")));
        assertEquals(2, last30DaysMap.get(Date.valueOf("2003-10-02")).getCounter());
        // 2003-10-01
        assertNotNull(last30DaysMap.get(Date.valueOf("2003-10-01")));
        assertEquals(1, last30DaysMap.get(Date.valueOf("2003-10-01")).getCounter());
    }
}