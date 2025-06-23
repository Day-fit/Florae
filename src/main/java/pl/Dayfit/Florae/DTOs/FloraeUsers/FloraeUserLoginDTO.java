package pl.Dayfit.Florae.DTOs.FloraeUsers;

import lombok.Data;

@Data
public class FloraeUserLoginDTO {
    private String username;
    private String email;
    private String password;

    private Boolean generateRefreshToken = false;
}