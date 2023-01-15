package me.erikpelli.jdigital.noncompliance.noncompliances;

import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
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

@WebMvcTest(NonCompliancesController.class)
class NonCompliancesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NonCompliancesService nonCompliancesService;

    @Test
    void getNonCompliancesListOrderedByMostRecent() throws Exception {
        Mockito.when(nonCompliancesService.getNonCompliances(Mockito.anyInt(), Mockito.anyInt(), Mockito.nullable(String.class))).thenAnswer((InvocationOnMock invocationOnMock) -> {
            int resultsPerPage = invocationOnMock.getArgument(0);
            int pageNumber = invocationOnMock.getArgument(1);
            String search = invocationOnMock.getArgument(2);

            if (search != null) {
                if (search.equals("I-1")) {
                    var nc = new NonCompliance(null, null, null, null);
                    nc.setCode(76);
                    return List.of(nc);
                }
                return List.of();
            }

            if (pageNumber != 1) {
                return List.of();
            }

            List<NonCompliance> result = new ArrayList<>(resultsPerPage);
            for (var i = 1; i <= resultsPerPage; i++) {
                var nc = new NonCompliance(null, null, null, null);
                nc.setCode(i);
                result.add(nc);
            }
            return result;
        });

        var getNonCompliancesRequest = MockMvcRequestBuilders
                .get("/noncompliances")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getNonCompliancesRequest.content("{\"resultsPerPage\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getNonCompliancesRequest.content("{\"pageNumber\": 2}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getNonCompliancesRequest.content("{\"search\": \"hello\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(getNonCompliancesRequest.content("{\"resultsPerPage\": 2, \"pageNumber\": 2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": []
                        }""", true));
        mockMvc.perform(getNonCompliancesRequest.content("{\"resultsPerPage\": 2, \"pageNumber\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": [
                                {"nonComplianceCode": 1},
                                {"nonComplianceCode": 2}
                            ]
                        }""", true));
        mockMvc.perform(getNonCompliancesRequest.content("{\"resultsPerPage\": 25, \"pageNumber\": 1, \"search\": \"notFound\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": []
                        }""", true));
        mockMvc.perform(getNonCompliancesRequest.content("{\"resultsPerPage\": 25, \"pageNumber\": 1, \"search\": \"I-1\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": [
                                {"nonComplianceCode": 76}
                            ]
                        }""", true));
    }

    @Test
    void returnTotalNumberNonComplianceByStatusAndLast30DaysStats() throws Exception {
        Mockito.when(nonCompliancesService.getNonCompliancesTotalStats()).thenReturn(Map.of(
                NonComplianceStatus.NEW, 0,
                NonComplianceStatus.ANALYSIS, 17,
                NonComplianceStatus.CHECK, 18,
                NonComplianceStatus.RESULT, 54
        ));
        Mockito.when(nonCompliancesService.getLastMonthStats(Mockito.isNull())).thenReturn(List.of(
                Map.of(
                        "date", "2022-10-10",
                        NonComplianceStatus.NEW, 2,
                        NonComplianceStatus.ANALYSIS, 3,
                        NonComplianceStatus.CHECK, 0,
                        NonComplianceStatus.RESULT, 1
                ),
                Map.of(
                        "date", "2022-10-09",
                        NonComplianceStatus.NEW, 5,
                        NonComplianceStatus.ANALYSIS, 1,
                        NonComplianceStatus.CHECK, 6,
                        NonComplianceStatus.RESULT, 2
                ),
                Map.of(
                        "date", "2022-10-08",
                        NonComplianceStatus.NEW, 1,
                        NonComplianceStatus.ANALYSIS, 4,
                        NonComplianceStatus.CHECK, 9,
                        NonComplianceStatus.RESULT, 10
                )
        ));

        var getNonCompliancesStatsRequest = MockMvcRequestBuilders
                .post("/noncompliances")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getNonCompliancesStatsRequest)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {
                                "totalNonCompliances": {
                                    "new": 0,
                                    "progress": 17,
                                    "review": 18,
                                    "closed": 54
                                },
                                "days": [
                                    {
                                        "date": "2022-10-10",
                                        "new": 2,
                                        "progress": 3,
                                        "review": 0,
                                        "closed": 1
                                    },
                                    {
                                        "date": "2022-10-09",
                                        "new": 5,
                                        "progress": 1,
                                        "review": 6,
                                        "closed": 2
                                    },
                                    {
                                        "date": "2022-10-08",
                                        "new": 1,
                                        "progress": 4,
                                        "review": 9,
                                        "closed": 10
                                    }
                                ]
                            }
                        }"""));
    }
}