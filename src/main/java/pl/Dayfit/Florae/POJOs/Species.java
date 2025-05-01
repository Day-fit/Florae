package pl.Dayfit.Florae.POJOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Species {
    private String scientificNameWithoutAuthor;
    private String scientificNameAuthorship;
}
