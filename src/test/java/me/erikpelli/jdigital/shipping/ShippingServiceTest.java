package me.erikpelli.jdigital.shipping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShippingServiceTest {
    @Mock
    private ShippingRepository shippingRepository;

    private ShippingService shippingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shippingService = new ShippingService(shippingRepository);
    }

    @Test
    void getLots() {
        var lots = List.of(
                new ShippingLot("shipping1", null, null),
                new ShippingLot("shipping2", null, null),
                new ShippingLot("shipping3", null, null),
                new ShippingLot("shipping4", null, null),
                new ShippingLot("shipping5", null, null)
        );
        Mockito.when(shippingRepository.findAll(Mockito.any(Pageable.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Pageable pageable = invocationOnMock.getArgument(0);
                    if (pageable.isUnpaged()) {
                        return lots;
                    }
                    int start = Math.min((int) pageable.getOffset(), lots.size());
                    int end = Math.min((start + pageable.getPageSize()), lots.size());
                    return lots.subList(start, end);
                });

        var allLots = shippingService.getLots(true, Optional.empty(), Optional.empty());
        assertEquals(Set.copyOf(lots), Set.copyOf(allLots));

        assertThrows(Exception.class, () -> shippingService.getLots(false, Optional.of(0), Optional.of(2)));
        assertThrows(Exception.class, () -> shippingService.getLots(false, Optional.of(1), Optional.of(0)));

        var firstTwo = shippingService.getLots(false, Optional.of(2), Optional.of(1));
        assertEquals(2, firstTwo.size());
        assertEquals(lots.subList(0, 2), firstTwo);

        var largPage = shippingService.getLots(false, Optional.of(25), Optional.of(1));
        assertEquals(lots.size(), largPage.size());
        assertEquals(lots, largPage);

        var emptyPage = shippingService.getLots(false, Optional.of(25), Optional.of(2));
        assertEquals(0, emptyPage.size());
        assertEquals(List.of(), emptyPage);

        var lastTwo = shippingService.getLots(false, Optional.of(3), Optional.of(2));
        assertEquals(2, lastTwo.size());
        assertEquals(lots.subList(3, 5), lastTwo);
    }
}