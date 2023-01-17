package me.erikpelli.jdigital.noncompliance.type;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NonComplianceTypeRepositoryTest {
    @Autowired
    private NonComplianceTypeRepository nonComplianceTypeRepository;

    @Test
    void findAll() {
        var types = List.of(
                new NonComplianceType(null, "NC1", "lorem ipsum"),
                new NonComplianceType(null, "NC2", "lorem ipsum"),
                new NonComplianceType(null, "NC3", "lorem ipsum")
        );
        nonComplianceTypeRepository.saveAll(types);

        var allTypes = nonComplianceTypeRepository.findAll();
        assertEquals(types.size(), allTypes.size());

        assertNotNull(allTypes.get(0).getCode());
        assertNotNull(allTypes.get(1).getCode());
        assertNotNull(allTypes.get(2).getCode());

        assertEquals(allTypes.get(0).getCode() + 1, allTypes.get(1).getCode());
        assertEquals(allTypes.get(1).getCode() + 1, allTypes.get(2).getCode());

        nonComplianceTypeRepository.deleteAll();
    }
}