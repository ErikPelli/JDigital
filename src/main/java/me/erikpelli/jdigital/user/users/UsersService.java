package me.erikpelli.jdigital.user.users;

import me.erikpelli.jdigital.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {
    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all the emails of registered users.
     *
     * @return List of email
     */
    public List<String> getAllEmails() {
        return userRepository.getAllEmails();
    }
}
