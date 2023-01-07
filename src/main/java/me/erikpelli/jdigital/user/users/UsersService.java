package me.erikpelli.jdigital.user.users;

import me.erikpelli.jdigital.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    /**
     * Get all the emails of registered users.
     *
     * @return List of email
     */
    public List<String> getAllEmails() {
        return usersRepository.getAllEmails();
    }
}
