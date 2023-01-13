package me.erikpelli.jdigital.shipping;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(ShippingController.class)
class ShippingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingService shippingService;

    @Test
    void getShippingLots() throws Exception {
        var lots = List.of(
                new ShippingLot("shipping1", null, Date.valueOf("2023-09-15")),
                new ShippingLot("shipping2", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping3", null, Date.valueOf("2000-10-13")),
                new ShippingLot("shipping4", null, Date.valueOf("0001-01-01")),
                new ShippingLot("shipping5", null, Date.valueOf("0001-01-01"))
        );
        Mockito.when(shippingService.getLots(Mockito.anyBoolean(), Mockito.any(Optional.class), Mockito.any(Optional.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    boolean getAll = invocationOnMock.getArgument(0);
                    if (getAll) {
                        return lots;
                    }

                    var resultsPerPage = ((Optional<?>) invocationOnMock.getArgument(1));
                    assertTrue(resultsPerPage.isPresent());
                    var pageNumber = ((Optional<?>) invocationOnMock.getArgument(2));
                    assertTrue(pageNumber.isPresent());
                    int offset = (Integer) resultsPerPage.get() * ((Integer) pageNumber.get() - 1);

                    int start = Math.min(offset, lots.size());
                    int end = Math.min((start + (Integer) resultsPerPage.get()), lots.size());
                    return lots.subList(start, end);
                });

        var getShippingRequest = MockMvcRequestBuilders
                .post("/details")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getShippingRequest).andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getShippingRequest.content("{\"limit\": \"true\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(getShippingRequest.content("{\"limit\": \"false\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [
                        {"shippingCode": "shipping1", "deliveryDate": "2023-09-15"},
                        {"shippingCode": "shipping2", "deliveryDate": "0001-01-01"},
                        {"shippingCode": "shipping3", "deliveryDate": "2000-10-13"},
                        {"shippingCode": "shipping4", "deliveryDate": "0001-01-01"},
                        {"shippingCode": "shipping5", "deliveryDate": "0001-01-01"}
                    ]
                }""", false));

        mockMvc.perform(getShippingRequest.content("""
                {
                    "limit": "true",
                    "resultsPerPage": 25,
                    "pageNumber": 1
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [
                        {"shippingCode": "shipping1", "deliveryDate": "2023-09-15"},
                        {"shippingCode": "shipping2", "deliveryDate": "0001-01-01"},
                        {"shippingCode": "shipping3", "deliveryDate": "2000-10-13"},
                        {"shippingCode": "shipping4", "deliveryDate": "0001-01-01"},
                        {"shippingCode": "shipping5", "deliveryDate": "0001-01-01"}
                    ]
                }""", false));

        mockMvc.perform(getShippingRequest.content("""
                {
                    "limit": "true",
                    "resultsPerPage": 1,
                    "pageNumber": "3"
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [{"shippingCode": "shipping3", "deliveryDate": "2000-10-13"}]
                }""", true));

        mockMvc.perform(getShippingRequest.content("""
                {
                    "limit": "true",
                    "resultsPerPage": 1,
                    "pageNumber": 25
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"success\": true, \"result\": []}", true));

        mockMvc.perform(getShippingRequest.content("""
                {
                    "limit": "true",
                    "resultsPerPage": 3,
                    "pageNumber": 2
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [
                        {"shippingCode": "shipping4", "deliveryDate": "0001-01-01"},
                        {"shippingCode": "shipping5", "deliveryDate": "0001-01-01"}
                    ]
                }""", false));
    }
}