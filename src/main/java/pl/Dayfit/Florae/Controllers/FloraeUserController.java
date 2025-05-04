package pl.Dayfit.Florae.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Services.FloraeUserService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FloraeUserController {
    private final FloraeUserService floraeUserService;

    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]+";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @PostMapping("/api/v1/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody FloraeUserRequestDTO floraeUserRequestDTO)
    {
        if (floraeUserRequestDTO.getUsername() == null ||floraeUserRequestDTO.getUsername().isBlank() || !floraeUserRequestDTO.getUsername().matches(USERNAME_REGEX) || floraeUserRequestDTO.getUsername().length() > FloraeUser.MAX_USERNAME_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username is not valid"));
        }

        if (floraeUserRequestDTO.getEmail() == null || floraeUserRequestDTO.getEmail().isBlank() || !floraeUserRequestDTO.getEmail().matches(EMAIL_REGEX) || floraeUserRequestDTO.getEmail().length() > FloraeUser.MAX_EMAIL_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email is not valid"));
        }

        if (floraeUserRequestDTO.getPassword() == null || floraeUserRequestDTO.getPassword().isBlank() || floraeUserRequestDTO.getPassword().getBytes(StandardCharsets.UTF_8).length > FloraeUser.MAX_PASSWORD_LENGTH)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Password is not valid"));
        }

        try {
            floraeUserService.registerUser(floraeUserRequestDTO);
        } catch(DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
}
