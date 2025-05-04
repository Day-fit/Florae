package pl.Dayfit.Florae.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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
