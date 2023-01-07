package me.erikpelli.jdigital.user.users;

import me.erikpelli.jdigital.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UsersRepositoryTest {
    @Autowired
    private UsersRepository usersRepository;

    @Test
    void getAllEmails() {
        List<String> emails = List.of("email1@gmail.com", "email2@gmail.com",
                "email3@gmail.com", "email4@gmail.com", "email5@gmail.com");
        for (var email : emails) {
            usersRepository.save(new User(email.toUpperCase(), email, "12345678"));
        }
        var allEmails = usersRepository.getAllEmails();
        assertEquals(emails.size(), allEmails.size());
        assertEquals(Set.copyOf(emails), Set.copyOf(allEmails));
    }
}
