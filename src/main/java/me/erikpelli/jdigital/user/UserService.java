package me.erikpelli.jdigital.user;

import me.erikpelli.jdigital.user.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;

    private UserSettingsService userSettingsService;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setUserSettingsService(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    /**
     * Get the user information from his email.
     *
     * @param email email of the user account to find
     * @return User entity object
     */
    public User getByEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is mandatory"
            );
        }

        var user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "user not found"
            );
        }
        return user;
    }

    /**
     * Check if the user with provided login data exists.
     *
     * @param email    email of the user account to find
     * @param password clear password
     * @return User exists boolean
     */
    public boolean userExists(String email, String password) {
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is mandatory"
            );
        }
        if (password == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "password is mandatory"
            );
        }
        var user = userRepository.findFirstByEmail(email);
        return user != null && user.passwordMatches(password);
    }

    /**
     * Save a new user inside the database.
     *
     * @param user data of the user to save
     */
    @Transactional
    public void saveNewUser(User user) {
        if (user.getEmail() == null || !user.hasPassword() || user.getFiscalCode() == null ||
                user.getFirstName() == null || user.getLastName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mandatory parameters are missing"
            );
        }
        if (user.getFiscalCode().length() != 16) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mismatched fiscal code length, it must be 16"
            );
        }
        userRepository.save(user);
        userSettingsService.resetSettings(user.getEmail());
    }
}
