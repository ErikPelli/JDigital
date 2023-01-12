package me.erikpelli.jdigital.user.settings;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserSettingsServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    private UserSettingsService userSettingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userSettingsService = new UserSettingsService(userRepository, companyRepository);

        var users = Map.of(
                "1@gmail.com", new User("AAAAAAAAAAAAAAAA", "1@gmail.com", "12345678", new UserSettings(new Company("VAT1", "", ""), "1", "2")),
                "2@gmail.com", new User("BBBBBBBBBBBBBBBB", "2@gmail.com", "aaaaaaaa", null),
                "3@gmail.com", new User("CCCCCCCCCCCCCCCC", "3@gmail.com", null, null)
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
                    if (vatNum.equals("VAT1")) {
                        return new Company("VAT1", "Hooli", "5154 State University Drive, Los Angeles");
                    }
                    return null;
                });
    }

    @Test
    void getByEmail() {
        assertThrows(Exception.class, () -> userSettingsService.getByEmail(null));
        assertThrows(Exception.class, () -> userSettingsService.getByEmail("notfound@gmail.com"));

        var nullSettings = userSettingsService.getByEmail("2@gmail.com");
        assertNull(nullSettings);

        var settings = userSettingsService.getByEmail("1@gmail.com");
        assertEquals("1", settings.getJob());
        assertEquals("2", settings.getRole());
    }

    /**
     * Clean the settings data for next assertion.
     *
     * @param email user identifier
     */
    private void setEmptySettings(String email) {
        var user = userRepository.findFirstByEmail(email);
        if (user == null) {
            return;
        }
        user.setSettings(new UserSettings());
        userRepository.save(user);
    }

    @Test
    void setSettings() {
        assertThrows(Exception.class, () -> userSettingsService.setSettings(null, Optional.empty(),
                Optional.empty(), Optional.empty()));
        assertThrows(Exception.class, () -> userSettingsService.setSettings("notfound@gmail.com", Optional.empty(),
                Optional.empty(), Optional.empty()));

        UserSettings result;

        // Overwrite some settings
        userSettingsService.setSettings("1@gmail.com", Optional.of("cool job"), Optional.empty(), Optional.empty());
        result = userSettingsService.getByEmail("1@gmail.com");
        assertEquals("cool job", result.getJob());
        assertEquals("2", result.getRole());
        assertEquals("VAT1", result.getEmployerCode());

        // User without settings
        userSettingsService.setSettings("2@gmail.com", Optional.of("cool job"), Optional.empty(), Optional.empty());
        result = userSettingsService.getByEmail("2@gmail.com");
        assertEquals("cool job", result.getJob());
        assertNull(result.getRole());
        assertNull(result.getEmployerCode());

        userSettingsService.setSettings("2@gmail.com", Optional.empty(), Optional.of("cool role"), Optional.empty());
        result = userSettingsService.getByEmail("2@gmail.com");
        assertEquals("cool job", result.getJob()); // state from precedent settings
        assertEquals("cool role", result.getRole());
        assertNull(result.getEmployerCode());

        setEmptySettings("2@gmail.com"); // Reset settings
        userSettingsService.setSettings("2@gmail.com", Optional.of("cool job"), Optional.of("cool role"), Optional.empty());
        result = userSettingsService.getByEmail("2@gmail.com");
        assertEquals("cool job", result.getJob());
        assertEquals("cool role", result.getRole());
        assertNull(result.getEmployerCode());

        userSettingsService.setSettings("3@gmail.com", Optional.of("cool job"), Optional.of("cool role"), Optional.empty());
        result = userSettingsService.getByEmail("3@gmail.com");
        assertEquals("cool job", result.getJob());
        assertEquals("cool role", result.getRole());
        assertNull(result.getEmployerCode());

        assertThrows(Exception.class, () -> userSettingsService.setSettings("3@gmail.com", Optional.empty(), Optional.empty(), Optional.of("VAT2")));

        setEmptySettings("3@gmail.com"); // Reset settings
        userSettingsService.setSettings("3@gmail.com", Optional.empty(), Optional.empty(), Optional.of("VAT1"));
        result = userSettingsService.getByEmail("3@gmail.com");
        assertNull(result.getJob());
        assertNull(result.getRole());
        assertEquals("VAT1", result.getEmployerCode());
    }

    @Test
    void resetSettings() {
        assertThrows(Exception.class, () -> userSettingsService.resetSettings("notfound@gmail.com"));
        userSettingsService.setDefaultCompany("VAT1");
        userSettingsService.setDefaultJob("job1");
        userSettingsService.setDefaultRole("role1");

        userSettingsService.resetSettings("1@gmail.com");

        var result = userRepository.findFirstByEmail("1@gmail.com");
        assertNotNull(result);

        assertEquals("VAT1", result.getSettings().getEmployerCode());
        assertEquals("job1", result.getSettings().getJob());
        assertEquals("role1", result.getSettings().getRole());

        userSettingsService.setDefaultCompany("VAT2");
        assertThrows(Exception.class, () -> userSettingsService.resetSettings("1@gmail.com"));
    }
}