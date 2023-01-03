package me.erikpelli.jdigital.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {
    @Nullable
    User findFirstByEmail(String email);

    @NonNull
    @Query("select u.email from User u")
    List<String> getAllEmails();
}
