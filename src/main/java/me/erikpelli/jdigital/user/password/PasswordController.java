package me.erikpelli.jdigital.user.password;

import com.fasterxml.jackson.annotation.JsonView;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserViews;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class PasswordController {
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    /**
     * GET /api/password
     *
     * @param userData email
     * @return isSet
     */
    @GetMapping("/password")
    public Map<String, Boolean> checkIfPasswordIsSet(
            @RequestBody(required = false)
            @JsonView(UserViews.UserEmail.class)
            User userData) {
        var email = (userData != null) ? userData.getEmail() : null;
        boolean result = passwordService.isPasswordSet(email);
        return Map.of("isSet", result);
    }

    /**
     * POST /api/password
     *
     * @param userData email, password
     */
    @PostMapping("/password")
    public Object replaceOldPasswordWithNewOne(
            @RequestBody(required = false)
            Map<String, String> userData) {
        var email = userData.get("email");
        var password = userData.get("password");
        if (password == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "you have to specify the new password"
            );
        }
        passwordService.replaceOldPassword(email, password);
        return Map.of();
    }

    /**
     * DELETE /api/password
     *
     * @param userData email
     */
    @DeleteMapping("/password")
    public Object deleteCurrentPassword(
            @RequestBody(required = false)
            @JsonView(UserViews.UserEmail.class)
            User userData) {
        var email = (userData != null) ? userData.getEmail() : null;
        passwordService.deletePassword(email);
        return Map.of();
    }
}
