package me.erikpelli.jdigital.user;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Entity
@Table(name = "Users")
public class User {
    private final static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * A unique 16 chars string that identifies a person in Italy.
     */
    @Id
    @Column(length = 16)
    @JsonView(UserViews.UserDetail.class)
    private String fiscalCode;

    /**
     * Email of the user.
     */
    @Column(nullable = false, unique = true)
    @Email
    @NotBlank
    @JsonView(UserViews.UserEmail.class)
    private String email;

    /**
     * Hashed password.
     */
    private String password;

    @JsonView(UserViews.UserDetail.class)
    private String firstName;

    @JsonView(UserViews.UserDetail.class)
    private String lastName;

    public User() {
    }

    public User(String fiscalCode, String email, String password) {
        this.fiscalCode = fiscalCode;
        this.email = email;
        setPassword(password);
    }

    public User(String fiscalCode, String email, String password, String firstName, String lastName) {
        this.fiscalCode = fiscalCode;
        this.email = email;
        setPassword(password);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Hash the password and save the result.
     *
     * @param password clear password to encode
     */
    public void setPassword(String password) {
        this.password = (password == null) ? null : passwordEncoder.encode(password);
    }

    public void setFiscalCode(String fiscalCode) {
        if(fiscalCode.length() != 16) {
            throw new IllegalArgumentException("Fiscal code must have 16 chars");
        }
        this.fiscalCode = fiscalCode;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean hasPassword() {
        return password != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return fiscalCode.equals(user.fiscalCode) && email.equals(user.email) &&
                Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fiscalCode, email, firstName, lastName);
    }

    public boolean passwordMatches(String password) {
        if (password == null) {
            return this.password == null;
        }
        return passwordEncoder.matches(password, this.password);
    }
}
