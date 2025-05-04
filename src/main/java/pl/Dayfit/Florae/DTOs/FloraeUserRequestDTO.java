package pl.Dayfit.Florae.DTOs;

import lombok.Data;

@Data
public class FloraeUserRequestDTO {
    private String username;
    private String email;
    private String password;
}
