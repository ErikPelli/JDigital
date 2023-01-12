package me.erikpelli.jdigital.company;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CompanyRepositoryTest {
    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void findFirstByVatNum() {
        var companies = List.of(
                new Company("VAT1", "Fake Name", "A Street"),
                new Company("VAT2", "Real Name", "B Street"),
                new Company("VAT3", "Name", "C Street")
        );
        companyRepository.saveAll(companies);

        var found = companyRepository.findFirstByVatNum("VAT3");
        assertEquals(companies.get(2), found);

        var notFound = companyRepository.findFirstByVatNum("VAT4");
        assertNull(notFound);
    }
}