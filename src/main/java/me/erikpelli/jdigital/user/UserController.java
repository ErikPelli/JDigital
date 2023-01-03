package me.erikpelli.jdigital.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/user
     *
     * @param userData email
     * @return fiscalCode, firstName, lastName
     */
    @GetMapping("/user")
    @JsonView(UserViews.UserDetail.class)
    public User getUserInformation(
            @RequestBody(required = false)
            @JsonView(UserViews.UserEmail.class)
            User userData) {
        var email = (userData != null) ? userData.getEmail() : null;
        return userService.getByEmail(email);
    }

    /**
     * POST /api/user
     *
     * @param loginData email, password
     * @return exists
     */
    @PostMapping("/user")
    public Map<String, Boolean> checkLoginData(
            @RequestBody(required = false)
            Map<String, String> loginData) {
        var email = loginData.get("email");
        var password = loginData.get("password");

        boolean result = userService.userExists(email, password);
        return Map.of("exists", result);
    }

    /**
     * PUT /api/user
     *
     * @param userData fiscalCode, firstName, lastName, email, password
     */
    @PutMapping("/user")
    public Object registerUser(
            @RequestBody(required = false)
            User userData) {
        userService.saveNewUser(userData);
        return Map.of();
    }
}
