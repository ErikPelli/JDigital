package me.erikpelli.jdigital.noncompliance.noncompliances;

import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.noncompliance.NonComplianceOrigin;
import me.erikpelli.jdigital.noncompliance.NonComplianceRepository;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NonCompliancesServiceTest {
    @Mock
    private NonComplianceRepository nonComplianceRepository;

    private NonCompliancesService nonCompliancesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nonCompliancesService = new NonCompliancesService(nonComplianceRepository);
    }

    @Test
    void getNonCompliances() {
        Mockito.when(nonComplianceRepository.findAll(Mockito.any(Pageable.class))).thenReturn(
                List.of(new NonCompliance(null, null, null, "findAllComment"))
        );
        Mockito.when(nonComplianceRepository.findByCommentContaining(Mockito.any(), Mockito.any())).thenReturn(
                List.of(new NonCompliance(null, null, null, "CommentContaining"))
        );
        Mockito.when(nonComplianceRepository.findByOriginAndCode(Mockito.any(NonComplianceOrigin.class), Mockito.anyInt(), Mockito.any()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    NonComplianceOrigin origin = invocationOnMock.getArgument(0);
                    Integer code = invocationOnMock.getArgument(1);
                    return List.of(new NonCompliance(null, null, origin, code.toString()));
                });

        assertThrows(Exception.class, () -> nonCompliancesService.getNonCompliances(1, 0, null));
        assertThrows(Exception.class, () -> nonCompliancesService.getNonCompliances(0, 1, null));

        var allNonCompliances = nonCompliancesService.getNonCompliances(1, 1, null);
        assertEquals(1, allNonCompliances.size());
        assertEquals("findAllComment", allNonCompliances.get(0).getComment());

        var commentNonCompliances = nonCompliancesService.getNonCompliances(1, 1, "hello");
        assertEquals(1, commentNonCompliances.size());
        assertEquals("CommentContaining", commentNonCompliances.get(0).getComment());

        var specificNonCompliance = nonCompliancesService.getNonCompliances(1, 1, "I-90");
        assertEquals(1, specificNonCompliance.size());
        assertEquals(NonComplianceOrigin.INTERNAL, specificNonCompliance.get(0).getOrigin());
        assertEquals("90", specificNonCompliance.get(0).getComment());

        var invalidSpecificNonCompliance = nonCompliancesService.getNonCompliances(1, 1, "Z-90");
        assertEquals(1, invalidSpecificNonCompliance.size());
        assertEquals("CommentContaining", invalidSpecificNonCompliance.get(0).getComment());
    }

    @Test
    void getNonCompliancesTotalStats() {
        Mockito.when(nonComplianceRepository.totalStatsByStatus()).thenReturn(Map.of(
                NonComplianceStatus.ANALYSIS, 5,
                NonComplianceStatus.CHECK, 4
        ));

        var result = nonCompliancesService.getNonCompliancesTotalStats();
        assertEquals(result.get(NonComplianceStatus.NEW), 0);
        assertEquals(result.get(NonComplianceStatus.ANALYSIS), 5);
        assertEquals(result.get(NonComplianceStatus.CHECK), 4);
        assertEquals(result.get(NonComplianceStatus.RESULT), 0);
    }

    @Test
    void getLastMonthStats() {
        Mockito.when(nonComplianceRepository.last30DaysStats(Mockito.any(), Mockito.any())).thenReturn(Map.of(
                Date.valueOf("2022-10-15"),
                Map.of(
                        NonComplianceStatus.ANALYSIS, 5,
                        NonComplianceStatus.CHECK, 4
                ),
                Date.valueOf("2022-10-13"),
                Map.of(
                        NonComplianceStatus.NEW, 14,
                        NonComplianceStatus.RESULT, 3
                ),
                Date.valueOf("2022-10-12"),
                Map.of(
                        NonComplianceStatus.NEW, 1,
                        NonComplianceStatus.ANALYSIS, 2,
                        NonComplianceStatus.CHECK, 3,
                        NonComplianceStatus.RESULT, 4
                )
        ));

        var todayDate = LocalDate.parse("2022-10-16");
        var lastMonthResult = nonCompliancesService.getLastMonthStats(todayDate);
        assertEquals(30, lastMonthResult.size());

        // 2022-10-16
        assertEquals(lastMonthResult.get(0).get("date"), "2022-10-16");
        assertEquals(0, lastMonthResult.get(0).get(NonComplianceStatus.NEW));
        assertEquals(0, lastMonthResult.get(0).get(NonComplianceStatus.ANALYSIS));
        assertEquals(0, lastMonthResult.get(0).get(NonComplianceStatus.CHECK));
        assertEquals(0, lastMonthResult.get(0).get(NonComplianceStatus.RESULT));
        // 2022-10-15
        assertEquals(lastMonthResult.get(1).get("date"), "2022-10-15");
        assertEquals(0, lastMonthResult.get(1).get(NonComplianceStatus.NEW));
        assertEquals(5, lastMonthResult.get(1).get(NonComplianceStatus.ANALYSIS));
        assertEquals(4, lastMonthResult.get(1).get(NonComplianceStatus.CHECK));
        assertEquals(0, lastMonthResult.get(1).get(NonComplianceStatus.RESULT));
        // 2022-10-14
        assertEquals(lastMonthResult.get(2).get("date"), "2022-10-14");
        assertEquals(0, lastMonthResult.get(2).get(NonComplianceStatus.NEW));
        assertEquals(0, lastMonthResult.get(2).get(NonComplianceStatus.ANALYSIS));
        assertEquals(0, lastMonthResult.get(2).get(NonComplianceStatus.CHECK));
        assertEquals(0, lastMonthResult.get(2).get(NonComplianceStatus.RESULT));
        // 2022-10-13
        assertEquals(lastMonthResult.get(3).get("date"), "2022-10-13");
        assertEquals(14, lastMonthResult.get(3).get(NonComplianceStatus.NEW));
        assertEquals(0, lastMonthResult.get(3).get(NonComplianceStatus.ANALYSIS));
        assertEquals(0, lastMonthResult.get(3).get(NonComplianceStatus.CHECK));
        assertEquals(3, lastMonthResult.get(3).get(NonComplianceStatus.RESULT));
        // 2022-10-12
        assertEquals(lastMonthResult.get(4).get("date"), "2022-10-12");
        assertEquals(1, lastMonthResult.get(4).get(NonComplianceStatus.NEW));
        assertEquals(2, lastMonthResult.get(4).get(NonComplianceStatus.ANALYSIS));
        assertEquals(3, lastMonthResult.get(4).get(NonComplianceStatus.CHECK));
        assertEquals(4, lastMonthResult.get(4).get(NonComplianceStatus.RESULT));

        // Check that all other days are empty
        for (var i = 5; i < lastMonthResult.size(); i++) {
            assertEquals(todayDate.minusDays(i).toString(), lastMonthResult.get(i).get("date"));
            assertEquals(0, lastMonthResult.get(i).get(NonComplianceStatus.NEW));
            assertEquals(0, lastMonthResult.get(i).get(NonComplianceStatus.ANALYSIS));
            assertEquals(0, lastMonthResult.get(i).get(NonComplianceStatus.CHECK));
            assertEquals(0, lastMonthResult.get(i).get(NonComplianceStatus.RESULT));
        }
    }
}