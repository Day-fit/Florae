package pl.Dayfit.Florae.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @OneToOne
    private Esp linkedEsp;

    @ManyToOne
    @JoinColumn(nullable = false)
    private FloraeUser linkedUser;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PlantRequirements requirements;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String primaryPhoto;
}