package me.erikpelli.jdigital.ticket.tickets;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.ticket.Ticket;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebMvcTest(TicketsController.class)
class TicketsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketsService ticketsService;

    @Test
    void getTicketsInPage() throws Exception {
        Mockito.when(ticketsService.getTickets(Mockito.anyInt(), Mockito.anyInt())).thenAnswer((InvocationOnMock invocationOnMock) -> {
            int resultsPerPage = invocationOnMock.getArgument(0);
            int pageNumber = invocationOnMock.getArgument(1);

            if (pageNumber != 1) {
                return List.of();
            }

            List<Ticket> result = new ArrayList<>(resultsPerPage);
            for (var i = 1; i <= resultsPerPage; i++) {
                var nc = new NonCompliance(null, null, null, null);
                nc.setCode(i);
                var ticket = new Ticket(new Company("c-"+i, null, null), nc, null, null, null);
                result.add(ticket);
            }
            return result;
        });

        var getTicketsRequest = MockMvcRequestBuilders
                .get("/tickets")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getTicketsRequest.content("{\"resultsPerPage\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getTicketsRequest.content("{\"pageNumber\": 2}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(getTicketsRequest.content("{\"resultsPerPage\": 2, \"pageNumber\": 2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": []
                        }""", true));
        mockMvc.perform(getTicketsRequest.content("{\"resultsPerPage\": 4, \"pageNumber\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": [
                                {"vatNum": "c-1", "nonComplianceCode": 1},
                                {"vatNum": "c-2", "nonComplianceCode": 2},
                                {"vatNum": "c-3", "nonComplianceCode": 3},
                                {"vatNum": "c-4", "nonComplianceCode": 4}
                            ]
                        }""", true));
    }

    @Test
    void getTicketsStatistics() throws Exception {
        Mockito.when(ticketsService.totalTickets()).thenReturn(Long.valueOf(37));
        Mockito.when(ticketsService.getLastMonthStats(Mockito.isNull())).thenReturn(List.of(
                Map.of(
                        "date", "2022-10-10",
                        "counter", 4
                ),
                Map.of(
                        "date", "2022-10-09",
                        "counter", 3
                ),
                Map.of(
                        "date", "2022-10-08",
                        "counter", 2
                )
        ));

        var getTicketsStatsRequest = MockMvcRequestBuilders
                .post("/tickets")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getTicketsStatsRequest)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {
                                "totalTickets": 37,
                                "days": [
                                    {
                                        "date": "2022-10-10",
                                        "counter": 4
                                    },
                                    {
                                        "date": "2022-10-09",
                                        "counter": 3
                                    },
                                    {
                                        "date": "2022-10-08",
                                        "counter": 2
                                    }
                                ]
                            }
                        }"""));
    }
}