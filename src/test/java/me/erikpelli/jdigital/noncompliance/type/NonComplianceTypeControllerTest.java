package me.erikpelli.jdigital.noncompliance.type;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(NonComplianceTypeController.class)
class NonComplianceTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NonComplianceTypeService nonComplianceTypeService;

    @Test
    void getAvailableNonComplianceTypes() throws Exception {
        var nonComplianceTypes = List.of(
                new NonComplianceType(1, "nc1", null),
                new NonComplianceType(2, "nc2", "foo bar"),
                new NonComplianceType(3, "nc3", null)
        );
        Mockito.when(nonComplianceTypeService.getPossibleNonCompliances()).thenReturn(nonComplianceTypes);

        var getTypesRequest = MockMvcRequestBuilders
                .put("/noncompliances")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getTypesRequest)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [
                        {"code": 1, "name": "nc1", "description": null},
                        {"code": 2, "name": "nc2", "description": "foo bar"},
                        {"code": 3, "name": "nc3", "description": null}
                    ]
                }""", false));
    }
}