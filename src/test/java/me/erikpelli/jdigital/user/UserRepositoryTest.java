package me.erikpelli.jdigital.user;

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

    @Test
    void findFirstByEmail() {
        var users = List.of(
            new User("AA", "1@gmail.com", "12345678"),
            new User("BB", "2@gmail.com", "aaaaaaaa"),
            new User("CC", "3@gmail.com", "bbbbbbbb")
        );
        userRepository.saveAll(users);

        var found = userRepository.findFirstByEmail("2@gmail.com");
        assertEquals(users.get(1), found);
        var notFound = userRepository.findFirstByEmail("4@gmail.com");
        assertNull(notFound);
    }
}