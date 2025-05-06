package pl.Dayfit.Florae.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user in the Florae system.
 * This class is used to map user-related data to the database.

 * Fields:
 * - {@code id}: A unique identifier for the user, automatically generated.
 * - {@code username}: The username chosen by the user, unique and required, with a maximum length of 64 characters.
 * - {@code email}: The email address of the user, unique and required, with a maximum length of 254 characters.
 * - {@code password}: The hashed password of the user, required, with a maximum length of 70 characters.
 * - {@code roles}: The roles assigned to the user for authorization purposes, with a default value of 'USER'.

 * Constants:
 * - {@code MAX_USERNAME_LENGTH}: Defines the maximum allowed length for the username.
 * - {@code MAX_PASSWORD_LENGTH}: Defines the maximum allowed length for the password.
 * - {@code MAX_EMAIL_LENGTH}: Defines the maximum allowed length for the email address.

 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity to map to the database.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter and setter methods for all fields.
 * - {@code @Id}: Denotes the primary key of the entity.
 * - {@code @GeneratedValue}: Specifies that the primary key is auto-generated.
 * - {@code @Column}: Configures the properties for the associated database columns such as uniqueness, nullability, and length.
 */
@Entity
@Getter
@Setter
public class FloraeUser {
    public static final int MAX_USERNAME_LENGTH = 64;
    public static final int MAX_PASSWORD_LENGTH = 70;
    public static final int MAX_EMAIL_LENGTH = 254;

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique=true, nullable=false, length = MAX_USERNAME_LENGTH)
    private String username;

    @Column(unique=true, nullable=false, length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(256) DEFAULT 'USER'")
    private String roles;
}
