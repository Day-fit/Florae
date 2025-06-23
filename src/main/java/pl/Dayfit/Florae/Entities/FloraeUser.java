package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a user in the Florae system.
 * This class is used to map user-related data to the database.
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code id}: A unique identifier for the user, automatically generated</li>
 *     <li>{@code username}: The username chosen by the user, unique and required, with a maximum length of 64 characters</li>
 *     <li>{@code email}: The email address of the user, unique and required, with a maximum length of 254 characters</li>
 *     <li>{@code password}: The hashed password of the user, required, with a maximum length of 70 characters</li>
 *     <li>{@code linkedPlants}: A list of plants associated with the user, mapped as a one-to-many relationship with cascading operations enabled</li>
 *     <li>{@code roles}: The roles assigned to the user for authorization purposes, with a default value of 'USER'</li>
 * </ul>
 *
 * <p>Constants:
 * <ul>
 *     <li>{@code MAX_USERNAME_LENGTH}: Defines the maximum allowed length for the username</li>
 *     <li>{@code MAX_PASSWORD_LENGTH}: Defines the maximum allowed length for the password</li>
 *     <li>{@code MAX_EMAIL_LENGTH}: Defines the maximum allowed length for the email address</li>
 * </ul>
 *
 * <p>Annotations:
 * <ul>
 *     <li>{@code @Entity}: Marks this class as a JPA entity to map to the database</li>
 *     <li>{@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter and setter methods for all fields</li>
 *     <li>{@code @Id}: Denotes the primary key of the entity</li>
 *     <li>{@code @GeneratedValue}: Specifies that the primary key is auto-generated</li>
 *     <li>{@code @Column}: Configures the properties for the associated database columns such as uniqueness, nullability, and length</li>
 * </ul>
 */
@Entity
@Getter
@Setter
public class FloraeUser {
    public static final int MAX_USERNAME_LENGTH = 64;
    public static final int MAX_PASSWORD_LENGTH = 128;
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

    @OneToMany(mappedBy = "linkedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plant> linkedPlants;

    @Column(nullable = false)
    private String roles = "USER";
}
