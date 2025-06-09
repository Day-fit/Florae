package pl.Dayfit.Florae.DTOs.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HandshakeDTO
{
    private String authorizationKey;
}