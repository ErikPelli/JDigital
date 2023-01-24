package me.erikpelli.jdigital.ticket;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.shipping.ShippingLot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.Objects;
import java.util.Optional;

@WebMvcTest(TicketController.class)
class TicketControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    void getDetailsAboutSingleTicket() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            TicketIdentifier key = invocationOnMock.getArgument(0);
            if (!key.companyId().equals("VAT1") || (key.nonComplianceId() != 1 && key.nonComplianceId() != 2)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            var customer = new Company("VAT1", "default company", "square 1234");
            return new Ticket(
                    customer,
                    null,
                    new ShippingLot("shipping1", customer, Date.valueOf("2020-01-01"), 10),
                    "description1",
                    (key.nonComplianceId() == 1) ? null : "answer1"
            );
        }).when(ticketService).getTicket(Mockito.any(TicketIdentifier.class));

        var getTicketDetailsRequest = MockMvcRequestBuilders
                .get("/ticket")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getTicketDetailsRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 3}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getTicketDetailsRequest.content("{\"vat\": \"VAT2\", \"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(getTicketDetailsRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {
                                "customerCompanyName": "default company",
                                "customerCompanyAddress": "square 1234",
                                "shippingCode": "shipping1",
                                "productQuantity": 10,
                                "problemDescription": "description1",
                                "status": "new"
                            }
                        }"""));

        mockMvc.perform(getTicketDetailsRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 2}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {
                                "customerCompanyName": "default company",
                                "customerCompanyAddress": "square 1234",
                                "shippingCode": "shipping1",
                                "productQuantity": 10,
                                "problemDescription": "description1",
                                "status": "progress",
                                "ticketAnswer": "answer1"
                            }
                        }"""));
    }

    @Test
    void createNewTicket() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            String vat = invocationOnMock.getArgument(0);
            String shipping = invocationOnMock.getArgument(1);
            Integer nc = invocationOnMock.getArgument(2);
            Optional<String> description = invocationOnMock.getArgument(3);

            if (!Objects.equals(vat, "VAT1") || !Objects.equals(shipping, "shipping1") || !Objects.equals(nc, 1)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            if (description.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "");
            }
            return null;
        }).when(ticketService).addTicket(Mockito.nullable(String.class), Mockito.nullable(String.class), Mockito.nullable(Integer.class), Mockito.any(Optional.class));

        var createTicketRequest = MockMvcRequestBuilders
                .put("/ticket")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1, \"shippingLot\":\"shipping2\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(createTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1, \"shippingLot\":\"shipping1\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(createTicketRequest.content("{\"vat\": \"VAT2\", \"nonCompliance\": 1, \"shippingLot\":\"shipping1\", \"description\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(createTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 2, \"shippingLot\":\"shipping1\", \"description\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(createTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1, \"shippingLot\":\"shipping1\", \"description\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }"""));
    }

    @Test
    void setAnswerToTicket() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            TicketIdentifier key = invocationOnMock.getArgument(0);
            if (!key.companyId().equals("VAT1") || key.nonComplianceId() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            return null;
        }).when(ticketService).answerToTicket(Mockito.any(TicketIdentifier.class), Mockito.anyString());

        var ticketSetAnswerRequest = MockMvcRequestBuilders
                .post("/ticket")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(ticketSetAnswerRequest.content("{\"vat\": \"VAT1\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(ticketSetAnswerRequest.content("{\"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(ticketSetAnswerRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(ticketSetAnswerRequest.content("{\"vat\": \"VAT2\", \"nonCompliance\": 1, \"answer\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(ticketSetAnswerRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 2, \"answer\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(ticketSetAnswerRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1, \"answer\": \"a\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }"""));
    }

    @Test
    void closeSingleTicket() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            TicketIdentifier key = invocationOnMock.getArgument(0);
            if (!key.companyId().equals("VAT1") || key.nonComplianceId() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            return null;
        }).when(ticketService).closeTicket(Mockito.any(TicketIdentifier.class));

        var closeTicketRequest = MockMvcRequestBuilders
                .delete("/ticket")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(closeTicketRequest.content("{\"vat\": \"VAT1\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(closeTicketRequest.content("{\"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(closeTicketRequest.content("{\"vat\": \"VAT2\", \"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(closeTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 2}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(closeTicketRequest.content("{\"vat\": \"VAT1\", \"nonCompliance\": 1}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "success": true,
                            "result": {}
                        }"""));
    }
}