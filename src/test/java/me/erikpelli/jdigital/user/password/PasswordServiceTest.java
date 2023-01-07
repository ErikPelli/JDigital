package me.erikpelli.jdigital.user.password;

import me.erikpelli.jdigital.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    @Mock
    private PasswordRepository passwordRepository;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(passwordRepository);
        var users = new ArrayList<>(List.of(
                new User("AA", "1@gmail.com", "12345678"),
                new User("BB", "2@gmail.com", "aaaaaaaa"),
                new User("CC", "3@gmail.com", null)
        ));
        Mockito.when(passwordRepository.save(Mockito.any(User.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    User toSave = invocationOnMock.getArgument(0);
                    users.add(toSave);
                    return toSave;
                });
        Mockito.when(passwordRepository.findFirstByEmail(Mockito.anyString()))
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
    void isPasswordSet() {
        assertThrows(Exception.class, () -> passwordService.isPasswordSet(null));
        assertThrows(Exception.class, () -> passwordService.isPasswordSet("fakeEmail@gmail.com"));
        assertTrue(passwordService.isPasswordSet("2@gmail.com"));
        assertFalse(passwordService.isPasswordSet("3@gmail.com"));
    }

    @Test
    void replaceOldPassword() {
        assertThrows(Exception.class, () -> passwordService.replaceOldPassword("", null));
        assertThrows(Exception.class, () -> passwordService.replaceOldPassword(null, ""));
        assertThrows(Exception.class, () -> passwordService.replaceOldPassword(null, null));
        assertDoesNotThrow(() -> passwordService.replaceOldPassword("1@gmail.com", ""));
        assertDoesNotThrow(() -> passwordService.replaceOldPassword("1@gmail.com", "password1"));
        assertDoesNotThrow(() -> passwordService.replaceOldPassword("3@gmail.com", "password2"));
        assertTrue(Objects.requireNonNull(passwordRepository.findFirstByEmail("1@gmail.com")).passwordMatches("password1"));
        assertTrue(Objects.requireNonNull(passwordRepository.findFirstByEmail("3@gmail.com")).passwordMatches("password2"));
    }

    @Test
    void deletePassword() {
        assertThrows(Exception.class, () -> passwordService.deletePassword(null));
        assertThrows(Exception.class, () -> passwordService.deletePassword("fakeEmail@gmail.com"));
        assertDoesNotThrow(() -> passwordService.deletePassword("2@gmail.com"));
        assertDoesNotThrow(() -> passwordService.isPasswordSet("3@gmail.com"));
        assertFalse(passwordService.isPasswordSet("2@gmail.com"));
        assertFalse(passwordService.isPasswordSet("3@gmail.com"));
    }
}