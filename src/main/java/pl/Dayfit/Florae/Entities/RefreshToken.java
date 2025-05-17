package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue
    Integer id;

    @JoinColumn(nullable = false)
    @ManyToOne //Many refresh tokens can be associated with one user (e.g., many devices are being used by one user)
    FloraeUser user;

    @Column(nullable = false)
    String token;

    @Column(nullable = false) //The expired token can be cleared from a database once it has expired
    Date expiryDate;

    @Column(nullable = false)
    Boolean isRevoked;
}
