package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an API key entity in the system.
 * This class is used to store and manage API key information, including its association
 * with users, creation date, revocation status, and linkage to specific FloraLink and FloraeUsers entities.

 * Fields:
 * - {@code id}: The unique identifier for the API key, automatically generated.
 * - {@code floraeUser}: The user associated with the API key. This defines the ownership or usage of the key.
 * - {@code createdDate}: The timestamp when the API key was created.
 * - {@code isRevoked}: A flag indicating whether the API key has been revoked. Defaults to false.
 * - {@code keyValue}: The string representation of the API key. This is a required field.
 * - {@code linkedFloraLink}: The FloraLink entity associated with the API key, representing a link to a specific ESP device.
 * - {@code linkedUser}: Another user, potentially linked to managing or operating the API key.
 * - {@code shortKey}: A shortened version of the API key string, used for display.

 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity to map to the database.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate getter and setter methods for all fields.
 * - {@code @Id}: Denotes the primary key of the entity.
 * - {@code @GeneratedValue}: Indicates the primary key value is automatically generated.
 * - {@code @Temporal}: Used to map the {@code createdDate} field to the {@code TIMESTAMP} type in the database.
 * - {@code @Column}: Configures database column properties for fields, such as nullability.
 * - {@code @ManyToOne}: Sets up many-to-one relationships with FloraeUsers entities.
 * - {@code @OneToOne}: Sets up a one-to-one relationship with the FloraLink entity.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue
    private Integer id;

    @JoinColumn(nullable = false)
    @ManyToOne(cascade = CascadeType.PERSIST)
    private FloraeUser floraeUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Instant createdDate;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    @Column(nullable = false)
    private String keyValue; //The API key String representation

    @Column(nullable = false, length = 12)
    private String shortKey;

    @OneToOne
    private FloraLink linkedFloraLink;

    @OneToOne
    @JoinColumn(nullable = false)
    private Plant linkedPlant;
}
