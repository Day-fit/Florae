package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    private FloraeUser floraeUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Instant createdDate;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    @Column(nullable = false)
    private String keyValue; //The API key String representation

    @OneToOne
    private FloraLink linkedFloraLink;

    @ManyToOne
    private FloraeUser linkedUser;
}
