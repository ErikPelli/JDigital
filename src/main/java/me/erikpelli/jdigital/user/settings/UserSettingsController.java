package me.erikpelli.jdigital.user.settings;

import com.fasterxml.jackson.annotation.JsonView;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserViews;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
public class UserSettingsController {
    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    /**
     * GET /api/settings
     *
     * @param userData email
     * @return job, role, company
     */
    @GetMapping("/settings")
    public Map<String, Object> getCurrentSettings(
            @RequestBody(required = false)
            @JsonView(UserViews.UserEmail.class)
            User userData) {
        var email = (userData != null) ? userData.getEmail() : null;
        var settings = userSettingsService.getByEmail(email);
        return Map.of(
                "job", Optional.ofNullable(settings.getJob()).orElse(""),
                "role", Optional.ofNullable(settings.getRole()).orElse(""),
                "company", Optional.ofNullable(settings.getEmployerCode()).orElse("")
        );
    }

    /**
     * POST /api/settings
     *
     * @param settingsData email, [optional] job role company
     */
    @PostMapping("/settings")
    public Object overwriteSomeSettings(
            @RequestBody(required = false)
            Map<String, String> settingsData) {
        if (settingsData == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "required parameters are missing");
        }
        var email = settingsData.get("email");
        var job = settingsData.get("job");
        var role = settingsData.get("role");
        var company = settingsData.get("company");
        userSettingsService.setSettings(
                email,
                Optional.ofNullable(job),
                Optional.ofNullable(role),
                Optional.ofNullable(company)
        );
        return Map.of();
    }

    /**
     * DELETE /api/settings
     *
     * @param userData email
     */
    @DeleteMapping("/settings")
    public Object resetUserSettings(
            @RequestBody(required = false)
            @JsonView(UserViews.UserEmail.class)
            User userData) {
        var email = (userData != null) ? userData.getEmail() : null;
        userSettingsService.resetSettings(email);
        return Map.of();
    }
}
