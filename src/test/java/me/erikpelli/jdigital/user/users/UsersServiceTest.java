package me.erikpelli.jdigital.user.users;

import me.erikpelli.jdigital.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsersServiceTest {
    @Mock
    private UserRepository userRepository;

    private UsersService usersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usersService = new UsersService(userRepository);
    }

    @Test
    void getAllEmails() {
        var emails = List.of(
                "1@gmail.com", "2@gmail.com", "3@gmail.com",
                "4@gmail.com", "5@gmail.com", "6@gmail.com"
        );
        Mockito.when(userRepository.getAllEmails()).thenReturn(emails);

        var result = usersService.getAllEmails();
        assertEquals(Set.of(emails), Set.of(result));
    }
}