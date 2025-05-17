package pl.Dayfit.Florae.DTOs;

import lombok.Data;

@Data
public class FloraeUserRegisterDTO {
    private String username;
    private String email;
    private String password;
}
