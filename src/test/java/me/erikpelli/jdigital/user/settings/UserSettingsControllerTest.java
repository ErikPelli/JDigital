package me.erikpelli.jdigital.user.settings;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(UserSettingsController.class)
class UserSettingsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private UserSettingsService userSettingsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CompanyRepository companyRepository;

    @Value("${user.default_job}")
    private String defaultJob;

    @Value("${user.default_role}")
    private String defaultRole;

    @Value("${user.default_company}")
    private String defaultCompany;

    @BeforeEach
    void setUp() {
        var users = Map.of(
                "1@gmail.com", new User("AAAAAAAAAAAAAAAA", "1@gmail.com", "12345678", new UserSettings(new Company("VAT2", "", ""), "1", "2")),
                "2@gmail.com", new User("BBBBBBBBBBBBBBBB", "2@gmail.com", "aaaaaaaa", null),
                "3@gmail.com", new User("CCCCCCCCCCCCCCCC", "3@gmail.com", null, null),
                "4@gmail.com", new User("DDDDDDDDDDDDDDDD", "4@gmail.com", null, new UserSettings(new Company("VAT2", "", ""), null, null))
        );
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    User toSave = invocationOnMock.getArgument(0);
                    var existentUser = users.get(toSave.getEmail());
                    existentUser.setFiscalCode(toSave.getFiscalCode());
                    existentUser.setEmail(toSave.getEmail());
                    existentUser.setName(toSave.getFirstName(), toSave.getLastName());
                    existentUser.setSettings(toSave.getSettings());
                    return existentUser;
                });
        Mockito.when(userRepository.findFirstByEmail(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String email = invocationOnMock.getArgument(0);
                    return users.get(email);
                });
        Mockito.when(companyRepository.findFirstByVatNum(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    String vatNum = invocationOnMock.getArgument(0);
                    if (vatNum.equals("IT895623147") || vatNum.equals("VAT2")) {
                        return new Company(vatNum, "Pied Piper", "5230 Newell Road, Palo Alto");
                    }
                    return null;
                });
    }

    @Test
    void getCurrentSettings() throws Exception {
        var getSettingsRequest = MockMvcRequestBuilders
                .get("/settings")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getSettingsRequest.content("{}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getSettingsRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(getSettingsRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(getSettingsRequest.content("{\"email\": \"1@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "company": "VAT2",
                        "job": "1",
                        "role": "2"
                    }
                }""", true));

        mockMvc.perform(getSettingsRequest.content("{\"email\": \"4@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {
                        "company": "VAT2",
                        "job": "",
                        "role": ""
                    }
                }""", true));
    }

    @Test
    void overwriteSomeSettings() throws Exception {
        var overwriteSettingsRequest = MockMvcRequestBuilders
                .post("/settings")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(overwriteSettingsRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(overwriteSettingsRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(overwriteSettingsRequest.content("""
                {
                    "email": "1@gmail.com",
                    "job": "manager",
                    "role": "engineering manager"
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));

        var resetSettingsUser1 = userRepository.findFirstByEmail("1@gmail.com");
        assertNotNull(resetSettingsUser1);
        assertNotNull(resetSettingsUser1.getSettings());
        assertEquals("VAT2", resetSettingsUser1.getSettings().getEmployerCode());
        assertEquals("manager", resetSettingsUser1.getSettings().getJob());
        assertEquals("engineering manager", resetSettingsUser1.getSettings().getRole());

        mockMvc.perform(overwriteSettingsRequest.content("""
                {
                    "email": "2@gmail.com",
                    "job": "employee",
                    "role": "developer",
                    "company": "VAT2"
                }"""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));

        var resetSettingsUser2 = userRepository.findFirstByEmail("2@gmail.com");
        assertNotNull(resetSettingsUser2);
        assertNotNull(resetSettingsUser2.getSettings());
        assertEquals("VAT2", resetSettingsUser2.getSettings().getEmployerCode());
        assertEquals("employee", resetSettingsUser2.getSettings().getJob());
        assertEquals("developer", resetSettingsUser2.getSettings().getRole());

        mockMvc.perform(overwriteSettingsRequest.content("{\"email\": \"3@gmail.com\", \"company\": \"VAT2\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));

        var resetSettingsUser3 = userRepository.findFirstByEmail("3@gmail.com");
        assertNotNull(resetSettingsUser3);
        assertNotNull(resetSettingsUser3.getSettings());
        assertEquals("VAT2", resetSettingsUser3.getSettings().getEmployerCode());
        assertNull(resetSettingsUser3.getSettings().getJob());
        assertNull(resetSettingsUser3.getSettings().getRole());

        mockMvc.perform(overwriteSettingsRequest.content("{\"email\": \"3@gmail.com\", \"company\": \"FAKE_VAT\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    void resetUserSettings() throws Exception {
        var resetUserRequest = MockMvcRequestBuilders
                .delete("/settings")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(resetUserRequest).andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(resetUserRequest.content("{\"email\": null}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        mockMvc.perform(resetUserRequest.content("{\"email\": \"fakeEmail@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(resetUserRequest.content("{\"email\": \"1@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));

        var resetSettingsUser1 = userRepository.findFirstByEmail("1@gmail.com");
        assertNotNull(resetSettingsUser1);
        assertNotNull(resetSettingsUser1.getSettings());
        assertEquals(defaultCompany, resetSettingsUser1.getSettings().getEmployerCode());
        assertEquals(defaultJob, resetSettingsUser1.getSettings().getJob());
        assertEquals(defaultRole, resetSettingsUser1.getSettings().getRole());

        userSettingsService.setDefaultCompany("VAT2");
        userSettingsService.setDefaultJob("1");
        userSettingsService.setDefaultRole("2");

        mockMvc.perform(resetUserRequest.content("{\"email\": \"3@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                {
                    "success": true,
                    "result": {}
                }""", true));

        var resetSettingsUser3 = userRepository.findFirstByEmail("3@gmail.com");
        assertNotNull(resetSettingsUser3);
        assertNotNull(resetSettingsUser3.getSettings());
        assertEquals("VAT2", resetSettingsUser3.getSettings().getEmployerCode());
        assertEquals("1", resetSettingsUser3.getSettings().getJob());
        assertEquals("2", resetSettingsUser3.getSettings().getRole());

        userSettingsService.setDefaultCompany("FAKE_VAT");
        mockMvc.perform(resetUserRequest.content("{\"email\": \"3@gmail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
}