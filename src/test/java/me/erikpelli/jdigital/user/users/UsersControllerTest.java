package me.erikpelli.jdigital.user.users;

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

@WebMvcTest(UsersController.class)
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @Test
    void getEmailsOfAllTheUsers() throws Exception {
        var emails = List.of(
                "1@gmail.com", "2@gmail.com", "3@gmail.com",
                "4@gmail.com", "5@gmail.com", "6@gmail.com"
        );
        Mockito.when(usersService.getAllEmails()).thenReturn(emails);

        var emailsOfAllUsersRequest = MockMvcRequestBuilders
                .get("/details")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(emailsOfAllUsersRequest)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": [
                        {"email": "1@gmail.com"},
                        {"email": "2@gmail.com"},
                        {"email": "3@gmail.com"},
                        {"email": "4@gmail.com"},
                        {"email": "5@gmail.com"},
                        {"email": "6@gmail.com"}
                    ]
                }""", false));
    }
}