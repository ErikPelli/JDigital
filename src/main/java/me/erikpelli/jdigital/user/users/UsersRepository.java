package me.erikpelli.jdigital.user.users;

import me.erikpelli.jdigital.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UsersRepository extends CrudRepository<User, String> {
    @NonNull
    @Query("select u.email from User u")
    List<String> getAllEmails();
}
