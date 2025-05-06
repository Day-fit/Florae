package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the requirements for a plant to thrive in terms of
 * various environmental and physical conditions.
 * This class is primarily used to store information about
 * the permissible range of light, temperature, humidity,
 * and soil moisture levels necessary for a plant's optimal growth.

 * Fields:
 * - id: A unique identifier for the PlantRequirements entity, auto-generated.
 * - pid: A unique string identifier for the plant requirements, required and unique.
 * - maxLightLux: The maximum light intensity (in lux) acceptable for the plant.
 * - minLightLux: The minimum light intensity (in lux) required for the plant.
 * - maxTemp: The maximum allowable temperature (in degrees Celsius) for the plant.
 * - minTemp: The minimum allowable temperature (in degrees Celsius) for the plant.
 * - maxEnvHumid: The maximum environmental humidity (in percentage) the plant can tolerate.
 * - minEnvHumid: The minimum environmental humidity (in percentage) the plant requires.
 * - maxSoilMoist: The maximum soil moisture level (in percentage) acceptable for the plant.
 * - minSoilMoist: The minimum soil moisture level (in percentage) required for the plant.

 * Annotations:
 * - {@code @Entity}: Specifies that this class is an entity managed by JPA.
 * - {@code @Getter} and {@code @Setter}: Lombok annotations to automatically generate
 *   getter and setter methods for the fields.
 * - {@code @Id}: Denotes the primary key of the entity.
 * - {@code @GeneratedValue}: Indicates the primary key value is automatically generated.
 * - {@code @Column}: Configures the properties and constraints of the mapped database columns,
 *   such as uniqueness and nullability.
 */
@Entity
@Getter
@Setter
public class PlantRequirements {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique=true, nullable=false)
    private String pid;

    private Integer maxLightLux;
    private Integer minLightLux;

    private Integer maxTemp;
    private Integer minTemp;

    private Integer maxEnvHumid;
    private Integer minEnvHumid;

    private Integer maxSoilMoist;
    private Integer minSoilMoist;
}
