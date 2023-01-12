package me.erikpelli.jdigital.user.settings;

import me.erikpelli.jdigital.company.CompanyRepository;
import me.erikpelli.jdigital.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserSettingsService {
    private final UserRepository userRepository;

    private final CompanyRepository companyRepository;

    @Value("${user.default_job}")
    private String defaultJob;

    @Value("${user.default_role}")
    private String defaultRole;

    @Value("${user.default_company}")
    private String defaultCompany;

    public UserSettingsService(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    protected void setDefaultJob(String defaultJob) {
        this.defaultJob = defaultJob;
    }

    protected void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    protected void setDefaultCompany(String defaultCompany) {
        this.defaultCompany = defaultCompany;
    }

    /**
     * Get settings of a user from its email.
     *
     * @param email email of the user
     * @return UserSettings object
     */
    public UserSettings getByEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is mandatory");
        }
        var user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
        return user.getSettings();
    }

    /**
     * Change the settings of a single user, empty optional parameters won't be changed.
     *
     * @param email   email of the user to change
     * @param job     new user job
     * @param role    new user role
     * @param company new user company
     */
    public void setSettings(String email, Optional<String> job, Optional<String> role, Optional<String> company) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is mandatory");
        }
        var user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }

        var currentSettings = Optional.ofNullable(user.getSettings()).orElseGet(UserSettings::new);
        job.ifPresent(currentSettings::setJob);
        role.ifPresent(currentSettings::setRole);
        if (company.isPresent()) {
            var findCompany = companyRepository.findFirstByVatNum(company.get());
            if (findCompany == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "company not found");
            }
            currentSettings.setEmployer(findCompany);
        }

        user.setSettings(currentSettings);
        userRepository.save(user);
    }

    /**
     * Reset the settings of a user with the default one.
     *
     * @param email email of the user to reset
     */
    public void resetSettings(String email) {
        setSettings(email, Optional.of(defaultJob), Optional.of(defaultRole), Optional.of(defaultCompany));
    }
}
