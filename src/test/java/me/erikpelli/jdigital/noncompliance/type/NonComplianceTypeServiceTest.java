package me.erikpelli.jdigital.noncompliance.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NonComplianceTypeServiceTest {
    @Mock
    private NonComplianceTypeRepository nonComplianceTypeRepository;

    private NonComplianceTypeService nonComplianceTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nonComplianceTypeService = new NonComplianceTypeService(nonComplianceTypeRepository);
    }

    @Test
    void getPossibleNonCompliances() {
        var nonComplianceTypes = List.of(
                new NonComplianceType(1, "nc1", null),
                new NonComplianceType(2, "nc2", "foo bar"),
                new NonComplianceType(3, "nc3", null)
        );
        Mockito.when(nonComplianceTypeRepository.findAll()).thenReturn(nonComplianceTypes);

        assertEquals(nonComplianceTypes.size(), nonComplianceTypeService.getPossibleNonCompliances().size());
        assertEquals(Set.of(nonComplianceTypes), Set.of(nonComplianceTypeService.getPossibleNonCompliances()));
    }
}