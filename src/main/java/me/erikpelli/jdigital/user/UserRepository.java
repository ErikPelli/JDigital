package me.erikpelli.jdigital.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

public interface UserRepository extends CrudRepository<User, String> {
    @Nullable
    User findFirstByEmail(String email);
}
