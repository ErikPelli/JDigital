package me.erikpelli.jdigital.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUser() {
        user = new User("AAA", "a@a.com", "safePassword");
    }

    @Test
    void setPassword() {
        user.setPassword("unsafePassword");
        assertTrue(user.passwordMatches("unsafePassword"));
        assertFalse(user.passwordMatches("safePassword"));
        assertDoesNotThrow(() -> user.setPassword(null));
    }

    @Test
    void hasPassword() {
        assertTrue(user.hasPassword());
        user.setPassword(null);
        assertFalse(user.hasPassword());

        User empty = new User();
        assertFalse(empty.hasPassword());
    }

    @Test
    void passwordMatches() {
        assertTrue(user.passwordMatches("safePassword"));

        User empty = new User();
        assertTrue(empty.passwordMatches(null));
    }
}