package me.erikpelli.jdigital.user.password;

import me.erikpelli.jdigital.user.User;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(PasswordController.class)
class PasswordControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordService passwordService;

    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        users = Map.of(
                "1@gmail.com", new User("1234", "1@gmail.com", "12345678", null),
                "2@gmail.com", new User("5678", "2@gmail.com", "aaaaaaaa", null, "John", "Doe"),
                "3@gmail.com", new User("91011", "3@gmail.com", null, null)
        );

        Mockito.when(passwordService.isPasswordSet(Mockito.nullable(String.class))).thenAnswer((InvocationOnMock invocationOnMock) -> {
            String email = invocationOnMock.getArgument(0);
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            var user = users.get(email);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            return user.hasPassword();
        });
    }

    @Test
    void checkIfPasswordIsSet() throws Exception {
        var userInformationRequest = MockMvcRequestBuilders
                .get("/password")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(userInformationRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"2@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "isSet": true
                    }
                }""", true));
        mockMvc.perform(userInformationRequest.content("{\"email\": \"3@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "isSet": false
                    }
                }""", true));
    }

    @Test
    void replaceOldPasswordWithNewOne() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            String email = invocationOnMock.getArgument(0);
            String password = invocationOnMock.getArgument(1);
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            var user = users.get(email);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            user.setPassword(password);
            return null;
        }).when(passwordService).replaceOldPassword(Mockito.nullable(String.class), Mockito.nullable(String.class));

        var userInformationRequest = MockMvcRequestBuilders
                .post("/password")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(userInformationRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\", \"password\": \"1234\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"2@gmail.com\", \"password\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"3@gmail.com\", \"password\": \"1234\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));
    }

    @Test
    void deleteCurrentPassword() throws Exception {
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            String email = invocationOnMock.getArgument(0);
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            var user = users.get(email);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
            }
            user.setPassword(null);
            return null;
        }).when(passwordService).deletePassword(Mockito.nullable(String.class));

        var userInformationRequest = MockMvcRequestBuilders
                .delete("/password")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(userInformationRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"2@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));
        mockMvc.perform(userInformationRequest.content("{\"email\": \"3@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));
        assertFalse(passwordService.isPasswordSet("2@gmail.com"));
        assertFalse(passwordService.isPasswordSet("3@gmail.com"));
    }
}