package me.erikpelli.jdigital.user.password;

import me.erikpelli.jdigital.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

public interface PasswordRepository extends CrudRepository<User, String> {
    @Nullable
    User findFirstByEmail(String email);
}
