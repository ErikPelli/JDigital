package me.erikpelli.jdigital.shipping;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {
    private final ShippingRepository shippingRepository;

    public ShippingService(ShippingRepository shippingRepository) {
        this.shippingRepository = shippingRepository;
    }

    /**
     * Get a list of shipping lots with an optional pagination in the request.
     *
     * @param getAllResults  if true, return all the results, else use the pagination
     * @param resultsPerPage number of entries for every page
     * @param pageNumber     number of the page to retrieve
     * @return List of shipping lots
     */
    public List<ShippingLot> getLots(boolean getAllResults, Optional<Integer> resultsPerPage, Optional<Integer> pageNumber) {
        if (getAllResults) {
            return shippingRepository.findAll(Pageable.unpaged());
        }

        if (resultsPerPage.isPresent() && pageNumber.isPresent() && resultsPerPage.get() > 0 && pageNumber.get() > 0) {
            var pagination = PageRequest.of(pageNumber.get(), resultsPerPage.get(), Sort.by("shippingDate").descending());
            return shippingRepository.findAll(pagination);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }
    }
}