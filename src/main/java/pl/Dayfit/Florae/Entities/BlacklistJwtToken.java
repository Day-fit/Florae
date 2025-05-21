package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
