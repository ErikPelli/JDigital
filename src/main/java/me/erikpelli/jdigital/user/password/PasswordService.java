package me.erikpelli.jdigital.user.password;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordService {
    private final PasswordRepository passwordRepository;

    public PasswordService(PasswordRepository passwordRepository) {
        this.passwordRepository = passwordRepository;
    }

    /**
     * Check if password is set.
     *
     * @param email user identifier
     * @return boolean result
     */
    public boolean isPasswordSet(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is mandatory");
        }
        var user = passwordRepository.findFirstByEmail(email);;
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
        return user.hasPassword();
    }

    /**
     * Replace password of a user.
     *
     * @param email    user identifier
     * @param password new password
     */
    public void replaceOldPassword(String email, String password) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is mandatory");
        }
        var user = passwordRepository.findFirstByEmail(email);;
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
        user.setPassword(password);
        passwordRepository.save(user);
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
