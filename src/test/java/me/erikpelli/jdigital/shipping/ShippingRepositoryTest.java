package me.erikpelli.jdigital.shipping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShippingRepositoryTest {
    @Autowired
    private ShippingRepository shippingRepository;

    @Test
    void findAll() {
        var lots = List.of(
                new ShippingLot("shipping1", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping2", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping3", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping4", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping5", null, Date.valueOf("0001-01-01"))
        );
        shippingRepository.saveAll(lots);

        var allShipping = shippingRepository.findAll(Pageable.unpaged());
        assertEquals(lots.size(), allShipping.size());
        assertEquals(Set.copyOf(lots), Set.copyOf(allShipping));

        var twoShipping = shippingRepository.findAll(Pageable.ofSize(2));
        assertEquals(2, twoShipping.size());

        var secondPage = shippingRepository.findAll(PageRequest.of(1, 2, Sort.by("shippingCode")));
        assertEquals(2, secondPage.size());
        assertEquals(lots.subList(2, 4), secondPage);

        var partialPage = shippingRepository.findAll(PageRequest.of(2, 2, Sort.by("shippingCode")));
        assertEquals(1, partialPage.size());
        assertEquals(lots.subList(4, 5), partialPage);
        shippingRepository.deleteAll();
    }

    @Test
    void findByShippingCode() {
        var shippingLot = new ShippingLot("shipping1", null, Date.valueOf("2022-01-01"));
        shippingRepository.save(shippingLot);

        var found = shippingRepository.findByShippingCode("shipping1");
        assertEquals(shippingLot, found);

        var notFound = shippingRepository.findByShippingCode("shipping2");
        assertNull(notFound);
        shippingRepository.deleteAll();
    }
}