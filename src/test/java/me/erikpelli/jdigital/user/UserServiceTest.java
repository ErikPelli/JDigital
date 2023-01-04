package me.erikpelli.jdigital.user;

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

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
        var users = new ArrayList<>(List.of(
                new User("AA", "1@gmail.com", "12345678"),
                new User("BB", "2@gmail.com", "aaaaaaaa"),
                new User("CC", "3@gmail.com", "bbbbbbbb")
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

        var userToFind = new User("AA", "1@gmail.com", "12345678");
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
                new User("1", "1@1.com", "1234", "John", "Doe"))
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
        assertDoesNotThrow(() -> userService.getByEmail("1@1.com"));
    }
}