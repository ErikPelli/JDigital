package me.erikpelli.jdigital.noncompliance;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

@WebMvcTest(NonComplianceController.class)
class NonComplianceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NonComplianceService nonComplianceService;

    @Test
    void getDetailsSingleNonCompliance() throws Exception {
        Mockito.when(nonComplianceService.getNonComplianceInfo(Mockito.anyInt())).thenReturn(
                Map.ofEntries(
                        Map.entry("origin", NonComplianceOrigin.INTERNAL),
                        Map.entry("nonComplianceType", 1),
                        Map.entry("nonComplianceDate", "2020-10-09"),
                        Map.entry("shippingLot", "1234"),
                        Map.entry("comment", "lorem ipsum"),
                        Map.entry("analysisEndDate", "2020-11-09")
                )
        );

        var getNonComplianceDetailsRequest = MockMvcRequestBuilders
                .get("/noncompliance")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getNonComplianceDetailsRequest)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getNonComplianceDetailsRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getNonComplianceDetailsRequest.content("{\"nonCompliance\": \"\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getNonComplianceDetailsRequest.content("{\"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {
                                "origin": "internal",
                                "nonComplianceType": 1,
                                "nonComplianceDate": "2020-10-09",
                                "shippingLot": "1234",
                                "comment": "lorem ipsum",
                                "analysisEndDate": "2020-11-09"
                            }
                        }""", true));
    }

    @Test
    void incrementNonComplianceStatus() throws Exception {
        var incrementNonComplianceStatusRequest = MockMvcRequestBuilders
                .post("/noncompliance")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(incrementNonComplianceStatusRequest)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(incrementNonComplianceStatusRequest.content("{\"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(incrementNonComplianceStatusRequest.content("{\"status\": \"analysys\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(incrementNonComplianceStatusRequest.content("{\"nonCompliance\": 1, \"status\": \"invalid\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(incrementNonComplianceStatusRequest.content("{\"nonCompliance\": 1, \"status\": \"analysis\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(incrementNonComplianceStatusRequest.content("{\"nonCompliance\": 1, \"status\": \"analysys\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }""", true));
        Mockito.verify(nonComplianceService, Mockito.times(1)).nextStatus(Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void createNonCompliance() throws Exception {
        var createNonComplianceRequest = MockMvcRequestBuilders
                .put("/noncompliance")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNonComplianceRequest)
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(createNonComplianceRequest.content("{\"nonComplianceOrigin\": \"internal\", \"nonComplianceType\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(createNonComplianceRequest.content("{\"shippingLot\": \"123\", \"nonComplianceType\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(createNonComplianceRequest.content("{\"nonComplianceOrigin\": \"invalid\", \"shippingLot\": \"123\", \"nonComplianceType\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(createNonComplianceRequest.content("{\"nonComplianceOrigin\": \"internal\", \"shippingLot\": \"123\", \"nonComplianceType\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }""", true));
        Mockito.verify(nonComplianceService, Mockito.times(1)).createNewNonCompliance(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(nonComplianceService, Mockito.times(0)).createNewNonCompliance(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        mockMvc.perform(createNonComplianceRequest.content("{\"nonComplianceOrigin\": \"internal\", \"shippingLot\": \"123\", \"nonComplianceType\": 1, \"comment\": \"\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }""", true));
        Mockito.verify(nonComplianceService, Mockito.times(1)).createNewNonCompliance(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}