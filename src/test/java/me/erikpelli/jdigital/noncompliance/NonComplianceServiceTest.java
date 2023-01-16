package me.erikpelli.jdigital.noncompliance;

import me.erikpelli.jdigital.noncompliance.state.NonComplianceState;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceType;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceTypeRepository;
import me.erikpelli.jdigital.shipping.ShippingLot;
import me.erikpelli.jdigital.shipping.ShippingRepository;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonComplianceServiceTest {
    @Mock
    private NonComplianceRepository nonComplianceRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NonComplianceTypeRepository nonComplianceTypeRepository;

    private NonComplianceService nonComplianceService;

    @BeforeAll
    void setUpImmutable() {
        MockitoAnnotations.openMocks(this);
        nonComplianceService = new NonComplianceService(nonComplianceRepository, shippingRepository, userRepository, nonComplianceTypeRepository);

        Mockito.when(shippingRepository.findByShippingCode(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String shippingCode = invocationOnMock.getArgument(0);
                    if (!shippingCode.equals("shipping1")) {
                        return null;
                    }
                    return new ShippingLot("shipping1", null);
                });
        Mockito.when(nonComplianceTypeRepository.findById(Mockito.anyInt()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Integer typeId = invocationOnMock.getArgument(0);
                    if (typeId != 1) {
                        return Optional.empty();
                    }
                    return Optional.of(new NonComplianceType(1, "default", null));
                });
        Mockito.when(userRepository.findById(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String fiscalCode = invocationOnMock.getArgument(0);
                    if (!fiscalCode.equals("1234567890123456")) {
                        return Optional.empty();
                    }
                    return Optional.of(new User("1234567890123456", "user@gmail.com", "", null));
                });
    }

    @BeforeEach
    void setUp() {
        var nonCompliances = new HashMap<>(Map.of(
                1, new NonCompliance(),
                2, new NonCompliance(),
                3, new NonCompliance()
        ));
        Mockito.when(nonComplianceRepository.findById(Mockito.anyInt()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Integer ncId = invocationOnMock.getArgument(0);
                    var nc = nonCompliances.get(ncId);
                    if (nc != null) {
                        nc.setCode(ncId);
                    }
                    return Optional.ofNullable(nc);
                });
        Mockito.when(nonComplianceRepository.save(Mockito.any(NonCompliance.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    NonCompliance nc = invocationOnMock.getArgument(0);
                    nonCompliances.put(nc.getCode() != null ? nc.getCode() : 0, nc);
                    return nc;
                });
    }

    @Test
    void getNonComplianceInfo() {
        assertTrue(nonComplianceRepository.findById(1).isPresent());
        assertTrue(nonComplianceRepository.findById(2).isPresent());
        assertTrue(nonComplianceRepository.findById(3).isPresent());

        assertTrue(nonComplianceTypeRepository.findById(1).isPresent());

        assertThrows(Exception.class, () -> nonComplianceService.getNonComplianceInfo(0));

        var nc1 = nonComplianceRepository.findById(1).get();
        nc1.setOrigin(NonComplianceOrigin.INTERNAL);
        nc1.setType(nonComplianceTypeRepository.findById(1).get());
        nc1.setDate(Date.valueOf("2020-09-17"));
        nc1.setLot(shippingRepository.findByShippingCode("shipping1"));
        nc1.setComment("");
        nc1.setNonComplianceState(new NonComplianceState(NonComplianceStatus.NEW));
        nonComplianceRepository.save(nc1);

        var result = assertDoesNotThrow(() -> nonComplianceService.getNonComplianceInfo(1));
        assertEquals(Map.of(
                "origin", NonComplianceOrigin.INTERNAL,
                "nonComplianceType", 1,
                "nonComplianceDate", "2020-09-17",
                "shippingLot", "shipping1"
        ), result);

        nc1.setComment("not empty");
        nc1.getNonComplianceState().setResultDescription("finished");
        nonComplianceRepository.save(nc1);

        var result2 = assertDoesNotThrow(() -> nonComplianceService.getNonComplianceInfo(1));
        assertEquals(Map.of(
                "origin", NonComplianceOrigin.INTERNAL,
                "nonComplianceType", 1,
                "nonComplianceDate", "2020-09-17",
                "shippingLot", "shipping1",
                "comment", "not empty",
                "result", "finished"
        ), result2);

        var nc2 = nonComplianceRepository.findById(2).get();
        nc2.setOrigin(NonComplianceOrigin.SUPPLIER);
        nc2.setType(nonComplianceTypeRepository.findById(1).get());
        nc2.setDate(Date.valueOf("2008-04-01"));
        nc2.setLot(shippingRepository.findByShippingCode("shipping1"));
        nc2.setComment("");
        nc2.setNonComplianceState(new NonComplianceState(NonComplianceStatus.RESULT, Date.valueOf("0001-10-09"), Date.valueOf("2002-01-08"), null, null));
        nonComplianceRepository.save(nc2);

        var result3 = assertDoesNotThrow(() -> nonComplianceService.getNonComplianceInfo(2));
        assertEquals(Map.of(
                "origin", NonComplianceOrigin.SUPPLIER,
                "nonComplianceType", 1,
                "nonComplianceDate", "2008-04-01",
                "shippingLot", "shipping1",
                "analysisEndDate", "0001-10-09",
                "checkEndDate", "2002-01-08"
        ), result3);

        assertTrue(userRepository.findById("1234567890123456").isPresent());
        nc2.getNonComplianceState().setManager(userRepository.findById("1234567890123456").get());
        nc2.getNonComplianceState().setAnalysisDate(null);
        nc2.getNonComplianceState().setCheckDate(null);
        nonComplianceRepository.save(nc2);

        var result4 = assertDoesNotThrow(() -> nonComplianceService.getNonComplianceInfo(2));
        assertEquals(Map.of(
                "origin", NonComplianceOrigin.SUPPLIER,
                "nonComplianceType", 1,
                "nonComplianceDate", "2008-04-01",
                "shippingLot", "shipping1",
                "managerEmail", "user@gmail.com"
        ), result4);
    }

    @Test
    void nextStatus() {
        assertTrue(nonComplianceRepository.findById(1).isPresent());
        nonComplianceRepository.findById(1).get().setNonComplianceState(new NonComplianceState(NonComplianceStatus.RESULT));

        assertThrows(Exception.class, () -> nonComplianceService.nextStatus(0, null, Optional.empty(), Optional.empty()));
        assertThrows(Exception.class, () -> nonComplianceService.nextStatus(1, NonComplianceStatus.ANALYSIS, Optional.empty(), Optional.empty()));
        assertThrows(Exception.class, () -> nonComplianceService.nextStatus(1, null, Optional.empty(), Optional.empty()));

        nonComplianceRepository.findById(1).get().setNonComplianceState(new NonComplianceState(NonComplianceStatus.CHECK));
        assertDoesNotThrow(() -> nonComplianceService.nextStatus(1, NonComplianceStatus.RESULT, Optional.empty(), Optional.empty()));
        assertEquals(NonComplianceStatus.RESULT, nonComplianceRepository.findById(1).get().getNonComplianceState().getStatus());
        assertEquals("", nonComplianceRepository.findById(1).get().getNonComplianceState().getResultDescription());
        nonComplianceRepository.findById(1).get().setNonComplianceState(new NonComplianceState(NonComplianceStatus.CHECK));
        assertDoesNotThrow(() -> nonComplianceService.nextStatus(1, NonComplianceStatus.RESULT, Optional.empty(), Optional.of("finished")));
        assertEquals(NonComplianceStatus.RESULT, nonComplianceRepository.findById(1).get().getNonComplianceState().getStatus());
        assertEquals("finished", nonComplianceRepository.findById(1).get().getNonComplianceState().getResultDescription());

        assertTrue(nonComplianceRepository.findById(2).isPresent());
        nonComplianceRepository.findById(2).get().setNonComplianceState(new NonComplianceState(NonComplianceStatus.NEW));
        assertDoesNotThrow(() -> nonComplianceService.nextStatus(2, NonComplianceStatus.ANALYSIS, Optional.empty(), Optional.empty()));
        assertEquals(NonComplianceStatus.ANALYSIS, nonComplianceRepository.findById(2).get().getNonComplianceState().getStatus());
        assertNotNull(nonComplianceRepository.findById(2).get().getNonComplianceState().getFormattedAnalysisDate());

        assertTrue(nonComplianceRepository.findById(3).isPresent());
        nonComplianceRepository.findById(3).get().setNonComplianceState(new NonComplianceState(NonComplianceStatus.ANALYSIS));
        assertThrows(Exception.class, () -> nonComplianceService.nextStatus(3, NonComplianceStatus.CHECK, Optional.of("invalidFiscalCode"), Optional.empty()));
        assertThrows(Exception.class, () -> nonComplianceService.nextStatus(3, NonComplianceStatus.CHECK, Optional.of("idNotFoundWLen16"), Optional.empty()));
        assertDoesNotThrow(() -> nonComplianceService.nextStatus(3, NonComplianceStatus.CHECK, Optional.of("1234567890123456"), Optional.empty()));
        assertEquals(NonComplianceStatus.CHECK, nonComplianceRepository.findById(3).get().getNonComplianceState().getStatus());
        assertNotNull(nonComplianceRepository.findById(3).get().getNonComplianceState().getFormattedCheckDate());
    }

    @Test
    void createNewNonComplianceWithComment() {
        nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", 1, "hello");
        assertTrue(nonComplianceRepository.findById(0).isPresent());
        assertEquals("hello", nonComplianceRepository.findById(0).get().getComment());
        assertEquals(NonComplianceOrigin.CUSTOMER, nonComplianceRepository.findById(0).get().getOrigin());
        assertTrue(nonComplianceTypeRepository.findById(1).isPresent());
        assertEquals(nonComplianceTypeRepository.findById(1).get(), nonComplianceRepository.findById(0).get().getType());
        assertEquals(shippingRepository.findByShippingCode("shipping1"), nonComplianceRepository.findById(0).get().getLot());

        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping2", 1, ""));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", 2, ""));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(null, "shipping1", 1, ""));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, null, 1, ""));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", null, ""));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", 1, null));
    }

    @Test
    void createNewNonCompliance() {
        nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", 1);
        assertTrue(nonComplianceRepository.findById(0).isPresent());
        assertEquals("", nonComplianceRepository.findById(0).get().getComment());

        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping2", 1));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", 2));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(null, "shipping1", 1));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, null, 1));
        assertThrows(Exception.class, () -> nonComplianceService.createNewNonCompliance(NonComplianceOrigin.CUSTOMER, "shipping1", null));
    }
}