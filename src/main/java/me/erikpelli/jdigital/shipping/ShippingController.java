package me.erikpelli.jdigital.shipping;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ShippingController {
    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    record ShippingDetailsInput(Boolean limit, Integer resultsPerPage, Integer pageNumber) {
    }

    /**
     * POST /api/details
     *
     * @param optionParams limit, resultsPerPage, pageNumber
     * @return list of shippingCode and deliveryDate
     */
    @PostMapping("/details")
    public List<Map<String, String>> getShippingLots(
            @RequestBody(required = false)
            ShippingDetailsInput optionParams) {
        if (optionParams.limit() == null ||
                (optionParams.limit() && (optionParams.resultsPerPage() == null || optionParams.pageNumber() == null))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pagination parameters");
        }

        var getAllResults = !optionParams.limit();
        Optional<Integer> resultsPerPage = optionParams.limit() ? Optional.of(optionParams.resultsPerPage()) : Optional.empty();
        Optional<Integer> pageNumber = optionParams.limit() ? Optional.of(optionParams.pageNumber()) : Optional.empty();

        var listOfLots = shippingService.getLots(getAllResults, resultsPerPage, pageNumber);

        return listOfLots.stream().map((ShippingLot lot) -> Map.of(
                "shippingCode", lot.getShippingCode(),
                "deliveryDate", lot.getShippingDate()
        )).toList();
    }
}
