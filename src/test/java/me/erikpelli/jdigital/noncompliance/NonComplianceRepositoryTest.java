package me.erikpelli.jdigital.noncompliance;

import me.erikpelli.jdigital.noncompliance.state.NonComplianceState;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonComplianceRepositoryTest {
    @Autowired
    private NonComplianceRepository nonComplianceRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private NonComplianceTypeRepository nonComplianceTypeRepository;

    private ShippingLot shippingLot;

    private NonComplianceType nonComplianceType;

    @BeforeAll
    void setUp() {
        shippingRepository.save(new ShippingLot("shipping1", null, Date.valueOf("2020-10-01")));
        nonComplianceTypeRepository.save(new NonComplianceType("broken part", null));
        shippingLot = shippingRepository.findByShippingCode("shipping1");

        nonComplianceType = nonComplianceTypeRepository.findById(1).orElse(null);
        assertNotNull(nonComplianceType);
    }

    @AfterAll
    void cleanUp() {
        nonComplianceRepository.deleteAll();
        shippingRepository.deleteAll();
        nonComplianceTypeRepository.deleteAll();
    }

    @Test
    void findByCode() {
        var firstId = nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null)).getCode();
        nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null));
        var lastId = nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null)).getCode();

        assertNull(nonComplianceRepository.findByCode(firstId - 1));
        assertNotNull(nonComplianceRepository.findByCode(firstId));
        assertNull(nonComplianceRepository.findByCode(lastId + 1));
    }

    @Test
    void findAll() {
        var nonCompliances = List.of(
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null)
        );
        nonComplianceRepository.saveAll(nonCompliances);

        var allNonCompliances = nonComplianceRepository.findAll(Pageable.unpaged());
        assertEquals(nonCompliances.size(), allNonCompliances.size());
        assertEquals(Set.copyOf(nonCompliances), Set.copyOf(allNonCompliances));

        var twoNC = nonComplianceRepository.findAll(Pageable.ofSize(2));
        assertEquals(2, twoNC.size());

        var partialPage = nonComplianceRepository.findAll(PageRequest.of(2, 2, Sort.by("code")));
        assertEquals(1, partialPage.size());
        assertEquals(nonCompliances.subList(4, 5), partialPage);
    }

    @Test
    void findByCommentContaining() {
        var nonCompliances = List.of(
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, "lorem ipsum dolorem"),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, "lorem"),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, "hhhellooo")
        );
        nonComplianceRepository.saveAll(nonCompliances);

        var helloComment = nonComplianceRepository.findByCommentContaining("hello", Pageable.unpaged());
        assertEquals(1, helloComment.size());
        assertEquals(nonCompliances.get(4), helloComment.get(0));

        var notFound = nonComplianceRepository.findByCommentContaining("helllo", Pageable.unpaged());
        assertNotNull(notFound);
        assertEquals(0, notFound.size());

        var ipsum = nonComplianceRepository.findByCommentContaining("ipsum", Pageable.unpaged());
        assertEquals(1, ipsum.size());
        assertEquals(nonCompliances.get(2), ipsum.get(0));

        var lorem = nonComplianceRepository.findByCommentContaining("lorem", Pageable.unpaged());
        assertEquals(2, lorem.size());
        assertEquals(Set.of(nonCompliances.subList(2, 4)), Set.of(lorem));

        var loremPaged = nonComplianceRepository.findByCommentContaining("lorem", PageRequest.of(1, 1, Sort.by("code")));
        assertEquals(1, loremPaged.size());
        assertEquals(nonCompliances.get(3), loremPaged.get(0));
    }

    @Test
    void findByOriginAndCode() {
        var firstId = nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null)).getCode();
        var secondObj = nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.CUSTOMER, null));
        nonComplianceRepository.save(new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null));

        var second = nonComplianceRepository.findByOriginAndCode(NonComplianceOrigin.CUSTOMER, secondObj.getCode(), Pageable.unpaged());
        assertEquals(1, second.size());
        assertEquals(secondObj, second.get(0));

        var notFound = nonComplianceRepository.findByOriginAndCode(NonComplianceOrigin.SUPPLIER, firstId, Pageable.unpaged());
        assertEquals(0, notFound.size());

        var nullCode = nonComplianceRepository.findByOriginAndCode(NonComplianceOrigin.INTERNAL, null, Pageable.unpaged());
        assertEquals(0, nullCode.size());
    }

    @Test
    void totalStatsByStatus() {
        var nonCompliances = List.of(
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, null)
        );
        nonCompliances.get(2).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(3).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(4).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(5).setNonComplianceState(new NonComplianceState(NonComplianceStatus.CHECK));

        // 0: NEW
        // 1: NEW
        // 2: ANALYSIS
        // 3: ANALYSIS
        // 4: ANALYSIS
        // 5: CHECK
        nonComplianceRepository.saveAll(nonCompliances);

        var stats = nonComplianceRepository.totalStatsByStatus();
        assertEquals(2, stats.get(NonComplianceStatus.NEW));
        assertEquals(3, stats.get(NonComplianceStatus.ANALYSIS));
        assertEquals(1, stats.get(NonComplianceStatus.CHECK));
        assertNull(stats.get(NonComplianceStatus.RESULT));
    }

    @Test
    void last30DaysStats() {
        var nonCompliances = List.of(
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-01"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-02"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-02"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-04"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-04"), null),
                new NonCompliance(shippingLot, nonComplianceType, NonComplianceOrigin.INTERNAL, Date.valueOf("2003-10-05"), null)
        );
        nonCompliances.get(2).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(3).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(4).setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        nonCompliances.get(5).setNonComplianceState(new NonComplianceState(NonComplianceStatus.CHECK));

        // 0: NEW
        // 1: NEW
        // 2: ANALYSIS
        // 3: ANALYSIS
        // 4: ANALYSIS
        // 5: CHECK
        nonComplianceRepository.saveAll(nonCompliances);

        var todayDate = LocalDate.parse("2003-10-05");

        var todayStats = nonComplianceRepository.last30DaysStats(todayDate.minusDays(1), todayDate);
        assertEquals(1, todayStats.size());
        // 2003-10-05
        assertNotNull(todayStats.get(Date.valueOf("2003-10-05")));
        assertEquals(1, todayStats.get(Date.valueOf("2003-10-05")).size());
        assertEquals(1, todayStats.get(Date.valueOf("2003-10-05")).get(NonComplianceStatus.CHECK));

        var last3Days = nonComplianceRepository.last30DaysStats(todayDate.minusDays(3), todayDate);
        assertEquals(2, last3Days.size());
        // 2003-10-05
        assertNotNull(last3Days.get(Date.valueOf("2003-10-05")));
        assertEquals(1, last3Days.get(Date.valueOf("2003-10-05")).size());
        assertEquals(1, last3Days.get(Date.valueOf("2003-10-05")).get(NonComplianceStatus.CHECK));
        // 2003-10-04
        assertNotNull(last3Days.get(Date.valueOf("2003-10-04")));
        assertEquals(1, last3Days.get(Date.valueOf("2003-10-04")).size());
        assertEquals(2, last3Days.get(Date.valueOf("2003-10-04")).get(NonComplianceStatus.ANALYSIS));
        // 2003-10-03
        assertNull(last3Days.get(Date.valueOf("2003-10-03")));

        var last30Days = nonComplianceRepository.last30DaysStats(todayDate.minusDays(30), todayDate);
        assertEquals(4, last30Days.size());
        // 2003-10-05
        assertNotNull(last30Days.get(Date.valueOf("2003-10-05")));
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-05")).size());
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-05")).get(NonComplianceStatus.CHECK));
        // 2003-10-04
        assertNotNull(last30Days.get(Date.valueOf("2003-10-04")));
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-04")).size());
        assertEquals(2, last30Days.get(Date.valueOf("2003-10-04")).get(NonComplianceStatus.ANALYSIS));
        // 2003-10-03
        assertNull(last30Days.get(Date.valueOf("2003-10-03")));
        // 2003-10-02
        assertNotNull(last30Days.get(Date.valueOf("2003-10-02")));
        assertEquals(2, last30Days.get(Date.valueOf("2003-10-02")).size());
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-02")).get(NonComplianceStatus.NEW));
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-02")).get(NonComplianceStatus.ANALYSIS));
        // 2003-10-01
        assertNotNull(last30Days.get(Date.valueOf("2003-10-01")));
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-01")).size());
        assertEquals(1, last30Days.get(Date.valueOf("2003-10-01")).get(NonComplianceStatus.NEW));
    }
}