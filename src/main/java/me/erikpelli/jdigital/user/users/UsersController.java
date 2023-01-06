package me.erikpelli.jdigital.user.users;

import com.fasterxml.jackson.annotation.JsonView;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserViews;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * GET /api/details
     *
     * @return list of objects with email parameter
     */
    @GetMapping("/details")
    @JsonView(UserViews.UserEmail.class)
    public List<User> getEmailsOfAllTheUsers() {
        var listOfEmails = usersService.getAllEmails();

        // Convert list of email to list of objects
        return listOfEmails.stream().map((String email) -> {
            var newUser = new User();
            newUser.setEmail(email);
            return newUser;
        }).toList();
    }
}
