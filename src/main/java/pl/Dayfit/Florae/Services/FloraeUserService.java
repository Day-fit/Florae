package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;


/**
 * Service class responsible for user-related operations in the Florae system.
 * Handles user registration, authentication, and token generation.

 * Responsibilities:
 * - Registering a new user with validation to ensure unique email and username.
 * - Validating user credentials during login.
 * - Generating JWT tokens for authenticated users.

 * Dependencies:
 * - {@code FloraeUserRepository}: Used for accessing and managing user data in the database.
 * - {@code BCryptPasswordEncoder}: Used for encrypting user passwords.
 * - {@code AuthenticationManager}: Used for authenticating user credentials.
 * - {@code JWTService}: Used for generating JSON Web Token (JWT) for user authentication.
 */
@Service
@RequiredArgsConstructor
public class FloraeUserService {
    private final FloraeUserRepository floraeUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;

    public void registerUser(FloraeUserRequestDTO floraeUserRequestDTO) throws DuplicateKeyException
    {
        if (floraeUserRepository.existsByEmailOrUsername(floraeUserRequestDTO.getEmail(), floraeUserRequestDTO.getUsername()))
        {
            throw new DuplicateKeyException("Username already exists or email already exists. Please try again with different username or email.");
        }

        FloraeUser floraeUser = new FloraeUser();
        floraeUser.setUsername(floraeUserRequestDTO.getUsername());
        floraeUser.setEmail(floraeUserRequestDTO.getEmail());
        floraeUser.setPassword(bCryptPasswordEncoder.encode(floraeUserRequestDTO.getPassword()));
        floraeUser.setRoles("USER");

        floraeUserRepository.save(floraeUser);
    }

    public boolean isValid(FloraeUserRequestDTO floraeUserRequestDTO) {
        return authManager.authenticate(new UsernamePasswordAuthenticationToken(floraeUserRequestDTO.getUsername(), floraeUserRequestDTO.getPassword())).isAuthenticated();
    }

    public String getToken(String username) {
        return jwtService.generateToken(username);
    }
}
