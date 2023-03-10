package me.erikpelli.jdigital.user;

import me.erikpelli.jdigital.user.settings.UserSettings;
import me.erikpelli.jdigital.user.settings.UserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsService userSettingsService;

    private UserService userService;

    private final UserSettings emptySettings = new UserSettings(null, "", "");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
        userService.setUserSettingsService(userSettingsService);
        var users = new ArrayList<>(List.of(
                new User("AA", "1@gmail.com", "12345678", emptySettings),
                new User("BB", "2@gmail.com", "aaaaaaaa", emptySettings),
                new User("CC", "3@gmail.com", "bbbbbbbb", emptySettings)
        ));
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    User toSave = invocationOnMock.getArgument(0);
                    users.add(toSave);
                    return toSave;
                });
        Mockito.when(userRepository.findFirstByEmail(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
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
    void getByEmail() {
        assertThrows(Exception.class, () -> userService.getByEmail(null));
        assertThrows(Exception.class, () -> userService.getByEmail("fakeEmail@gmail.com"));

        var userToFind = new User("AA", "1@gmail.com", "12345678", emptySettings);
        assertEquals(userToFind, userService.getByEmail(userToFind.getEmail()));
    }

    @Test
    void userExists() {
        assertThrows(Exception.class, () -> userService.userExists(null, null));
        assertThrows(Exception.class, () -> userService.userExists("", null));
        assertThrows(Exception.class, () -> userService.userExists(null, ""));
        assertDoesNotThrow(() -> userService.userExists("", ""));

        assertFalse(userService.userExists("fakeEmail@gmail.com", "1234"));
        assertFalse(userService.userExists("1@gmail.com", "87654321"));
        assertTrue(userService.userExists("1@gmail.com", "12345678"));
    }

    @Test
    void saveNewUser() {
        assertThrows(Exception.class, () -> userService.saveNewUser(
                new User("1", "1@1.com", "1234", emptySettings, "John", "Doe"))
        );

        var user = new User();
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setEmail("1@1.com");
        user.setPassword("1234");
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setName("John", "Doe");
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setFiscalCode("1234567890123456");

        assertDoesNotThrow(() -> userService.saveNewUser(user));
        Mockito.verify(userSettingsService, Mockito.times(1)).resetSettings(Mockito.anyString());
        assertDoesNotThrow(() -> userService.getByEmail("1@1.com"));
    }
}