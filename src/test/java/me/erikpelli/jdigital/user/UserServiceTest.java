package me.erikpelli.jdigital.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        var users = List.of(
                new User("AA", "1@gmail.com", "12345678"),
                new User("BB", "2@gmail.com", "aaaaaaaa"),
                new User("CC", "3@gmail.com", "bbbbbbbb")
        );
        userRepository.saveAll(users);
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
        var user = new User();
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setEmail("1@1.com");
        user.setPassword("1234");
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setName("John", "Doe");
        assertThrows(Exception.class, () -> userService.saveNewUser(user));
        user.setFiscalCode("1234567890123456");
        assertDoesNotThrow(() -> userService.saveNewUser(user));

        assertThrows(Exception.class, () -> userService.saveNewUser(new User("1", "1@1.com", "1234", "John", "Doe")));
    }
}