package me.erikpelli.jdigital.user;

import me.erikpelli.jdigital.user.settings.UserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSettingsService userSettingsService;

    @SpyBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var users = new ArrayList<>(List.of(
                new User("AA", "1@gmail.com", "12345678", null),
                new User("BB", "2@gmail.com", "aaaaaaaa", null, "John", "Doe"),
                new User("CC", "3@gmail.com", "bbbbbbbb", null)
        ));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer((InvocationOnMock invocationOnMock) -> {
            User toSave = invocationOnMock.getArgument(0);
            users.add(toSave);
            return toSave;
        });
        Mockito.when(userRepository.findFirstByEmail(Mockito.anyString())).thenAnswer((InvocationOnMock invocationOnMock) -> {
            String email = invocationOnMock.getArgument(0);
            for (var user : users) {
                if (user.getEmail().equals(email)) {
                    return user;
                }
            }
            return null;
        });
    }

    @Test
    void getUserInformation() throws Exception {
        var userInformationRequest = MockMvcRequestBuilders
                .get("/user")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(userInformationRequest)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{invalidJson}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"1@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "fiscalCode": "AA",
                        "firstName": null,
                        "lastName": null
                    }
                }""", true));
        mockMvc.perform(userInformationRequest.content("{\"email\": \"2@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "fiscalCode": "BB",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                }""", true));
    }

    @Test
    void checkLoginData() throws Exception {
        var userInformationRequest = MockMvcRequestBuilders
                .post("/user")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(userInformationRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\", \"password\": null}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(userInformationRequest.content("{\"email\": \"fakeEmail@gmail.com\", \"password\": \"12345678\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "exists": false
                    }
                }""", true));
        mockMvc.perform(userInformationRequest.content("{\"email\": \"1@gmail.com\", \"password\": \"12345678\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "exists": true
                    }
                }""", true));
        mockMvc.perform(userInformationRequest.content("{\"email\": \"1@gmail.com\", \"password\": \"87654321\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "exists": false
                    }
                }""", true));
    }

    @Test
    void registerUser() throws Exception {
        var userInformationRequest = MockMvcRequestBuilders
                .put("/user")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(userInformationRequest.content("{\"email\": \"newEmail@gmail.com\", \"password\": \"12345678\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        mockMvc.perform(userInformationRequest.content("""
                {
                    "fiscalCode": "12345678",
                    "email": "new@gmail.com",
                    "firstName": "Alice",
                    "lastName": "Bob",
                    "password": "12345678"
                }"""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        mockMvc.perform(userInformationRequest.content("""
                {
                    "fiscalCode": "1234567812345678",
                    "email": "new@gmail.com",
                    "firstName": "Alice",
                    "lastName": "Bob",
                    "password": "12345678"
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));
        assertNotNull(userRepository.findFirstByEmail("new@gmail.com"));
    }
}