package me.erikpelli.jdigital.user.password;

import me.erikpelli.jdigital.user.UserRepository;
import me.erikpelli.jdigital.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final UserRepository userRepository;
    private final UserService userService;

    public PasswordService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Check if password is set.
     *
     * @param email user identifier
     * @return boolean result
     */
    public boolean isPasswordSet(String email) {
        var user = userService.getByEmail(email);
        return user.hasPassword();
    }

    /**
     * Replace password of a user.
     *
     * @param email    user identifier
     * @param password new password
     */
    public void replaceOldPassword(String email, String password) {
        var user = userService.getByEmail(email);
        user.setPassword(password);
        userRepository.save(user);
    }

    /**
     * Delete password of a user.
     *
     * @param email user identifier
     */
    public void deletePassword(String email) {
        replaceOldPassword(email, null);
    }
}
