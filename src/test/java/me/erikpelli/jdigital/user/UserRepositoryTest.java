package me.erikpelli.jdigital.user;

import me.erikpelli.jdigital.user.settings.UserSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private final UserSettings emptySettings = new UserSettings(null, "", "");

    @Test
    void findFirstByEmail() {
        var users = List.of(
            new User("AA", "1@gmail.com", "12345678", emptySettings),
            new User("BB", "2@gmail.com", "aaaaaaaa", emptySettings),
            new User("CC", "3@gmail.com", "bbbbbbbb", emptySettings)
        );
        userRepository.saveAll(users);

        var found = userRepository.findFirstByEmail("2@gmail.com");
        assertEquals(users.get(1), found);
        var notFound = userRepository.findFirstByEmail("4@gmail.com");
        assertNull(notFound);
    }

    @Test
    void getAllEmails() {
        List<String> emails = List.of("email1@gmail.com", "email2@gmail.com",
                "email3@gmail.com", "email4@gmail.com", "email5@gmail.com");
        for (var email : emails) {
            userRepository.save(new User(email.toUpperCase(), email, "12345678", emptySettings));
        }
        var allEmails = userRepository.getAllEmails();
        assertEquals(emails.size(), allEmails.size());
        assertEquals(Set.copyOf(emails), Set.copyOf(allEmails));
    }
}