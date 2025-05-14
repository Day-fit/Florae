package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;

import java.security.SecureRandom;
import java.util.Base64;


/**
 * Service class responsible for user-related operations in the Florae system.
 * Handles user registration, authentication, and token generation.

 * <p>Responsibilities:</p>
 * <ul>
 *  <li> Registering a new user with validation to ensure a unique email and username.</li>
 *  <li> Validating user credentials during login.</li>
 *  <li> Generating JWT tokens for authenticated users.</li>
 * </ul>

 * <p>Dependencies:</p>
 * <ul>
 *  <li> {@code FloraeUserRepository}: Used for accessing and managing user data in the database.</li>
 *  <li> {@code BCryptPasswordEncoder}: Used for encrypting user passwords.</li>
 *  <li> {@code AuthenticationManager}: Used for authenticating user credentials.</li>
 *  <li> {@code JWTService}: Used for generating JSON Web Token (JWT) for user authentication.</li>
 *  <li> {@code SecureRandom}: Used for generating random salt values for user passwords.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FloraeUserService {
    private final FloraeUserRepository floraeUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final SecureRandom secureRandom;

    public void registerUser(FloraeUserRequestDTO floraeUserRequestDTO) throws DuplicateKeyException
    {
        if (floraeUserRepository.existsByEmailOrUsername(floraeUserRequestDTO.getEmail(), floraeUserRequestDTO.getUsername()))
        {
            throw new DuplicateKeyException("Username already exists or email already exists. Please try again with different username or email.");
        }

        FloraeUser floraeUser = new FloraeUser();
        floraeUser.setUsername(floraeUserRequestDTO.getUsername());
        floraeUser.setEmail(floraeUserRequestDTO.getEmail());
        floraeUser.setSalt(generateSalt());
        floraeUser.setPassword(bCryptPasswordEncoder.encode(floraeUserRequestDTO.getPassword() + floraeUser.getSalt()));
        floraeUser.setRoles("USER");

        floraeUserRepository.save(floraeUser);
    }

    public boolean isValid(FloraeUserRequestDTO floraeUserRequestDTO) {
        FloraeUser floraeUser = floraeUserRepository.findByUsername(floraeUserRequestDTO.getUsername());

        try {
            return authManager.authenticate(new UsernamePasswordAuthenticationToken(floraeUserRequestDTO.getUsername(), floraeUserRequestDTO.getPassword() + floraeUser.getSalt())).isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }

    public String getToken(String username) {
        return jwtService.generateToken(username);
    }

    private String generateSalt()
    {
        byte[] salt = new byte[44];
        secureRandom.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }
}
