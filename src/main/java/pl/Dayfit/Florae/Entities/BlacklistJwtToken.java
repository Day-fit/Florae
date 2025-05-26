package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Represents a JWT token that has been blacklisted in the system.
 * This class is used to manage and persist information about blacklisted
 * JWT tokens, including the token value and its expiry date.

 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity that will be mapped to a database table.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations that automatically generate
 *   getters and setters for all fields.
 * - {@code @Id}: Denotes the primary key of the entity.
 * - {@code @GeneratedValue}: Indicates that the primary key value is automatically generated.
 * - {@code @Column}: Configures the properties of the associated database columns,
 *   such as nullability.

 * Fields:
 * - {@code id}: The unique identifier for the blacklisted token, automatically generated.
 * - {@code token}: The JWT token string that has been blacklisted, which cannot be null.
 * - {@code expiryDate}: The expiration date of the blacklisted token, which must also be non-null.
 */
@Entity
@Getter
@Setter
public class BlacklistJwtToken {
    @Id
    @GeneratedValue
    Integer id;

    @Column(nullable = false)
    String token;

    @Column(nullable = false)
    Date expiryDate;
}
