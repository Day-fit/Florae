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
    private String slug;

    @OneToOne
    private Esp linked_esp;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PlantRequirements requirements;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String primaryPhoto;
}