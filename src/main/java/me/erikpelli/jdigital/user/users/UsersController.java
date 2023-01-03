package me.erikpelli.jdigital.user.users;

import com.fasterxml.jackson.annotation.JsonView;
import me.erikpelli.jdigital.user.User;
import me.erikpelli.jdigital.user.UserViews;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        var newList = new ArrayList<User>(listOfEmails.size());

        // Convert list of email to list of objects
        for (String email : listOfEmails) {
            var newUser = new User();
            newUser.setEmail(email);
            newList.add(newUser);
        }

        return newList;
    }
}
