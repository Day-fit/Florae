package pl.Dayfit.Florae.DTOs.FloraeUsers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FloraeUserResponseDTO {
    private String username;
    private String email;
}
