package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a plant entity in the system.
 * This class is used to map the plant-related data to the database
 * and associate it with other entities such as users, ESP devices,
 * and plant requirements.
 * The Plant entity includes information about the plant's species,
 * photos, associated ESP device, user ownership, and its specific
 * requirements for optimal growth.
 * Annotations:
 * - {@code @Entity}: Specifies that this class is a JPA entity.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations used
 *   to automatically generate getter and setter methods for the fields.
 */
@Getter
@Setter
@Entity
public class Plant {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, length = 50)
    private String speciesName;

    @Column(nullable = false, length = 50)
    private String pid;

    @OneToOne(cascade = CascadeType.DETACH)
    private FloraLink linkedFloraLink;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private FloraeUser linkedUser;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String primaryPhoto;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PlantRequirements requirements;
}