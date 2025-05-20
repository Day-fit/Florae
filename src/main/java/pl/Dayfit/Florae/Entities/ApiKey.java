package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class ApiKey {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Instant createdDate;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    @Column(nullable = false)
    private String value; //The API key String representation

    @OneToOne
    private FloraLink linkedFloraLink;
}
