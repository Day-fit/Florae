package pl.Dayfit.Florae.Services.Auth.JWT;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUsers.FloraeUserLoginDTO;
import pl.Dayfit.Florae.DTOs.FloraeUsers.FloraeUserRegisterDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;

/**
 * Service class responsible for user-related operations in the Florae system.
 * Handles user registration, authentication, and token generation.

 * <p>Responsibilities:</p>
 * <ul>
 *  <li> Registering a new user with validation to ensure a unique email and username.</li>
 *  <li> Validating user credentials during login.</li>
 *  <li> Generating JWT tokens for authenticated users.</li>
 *  <li> Generating new refresh tokens for authenticated users.</li>
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
    private final FloraeUserCacheService floraeUserCacheService;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;

    public static final int ACCESS_TOKEN_EXPIRATION_TIME = 30; //In minutes
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 14; //In days

    public void registerUser(FloraeUserRegisterDTO floraeUserRegisterDTO) throws DuplicateKeyException
    {
        if (floraeUserCacheService.findByEmailOrUsername(floraeUserRegisterDTO.getEmail(), floraeUserRegisterDTO.getUsername()) != null)
        {
            throw new DuplicateKeyException("Username already exists or email already exists. Please try again with different username or email.");
        }

        FloraeUser floraeUser = new FloraeUser();
        floraeUser.setUsername(floraeUserRegisterDTO.getUsername().toLowerCase());
        floraeUser.setEmail(floraeUserRegisterDTO.getEmail().toLowerCase());
        floraeUser.setPassword(bCryptPasswordEncoder.encode(floraeUserRegisterDTO.getPassword()));
        floraeUser.setRoles("USER");

        floraeUserCacheService.saveFloraeUser(floraeUser);
    }

    public boolean isValid(FloraeUserLoginDTO floraeUserLoginDTO) {
        FloraeUser floraeUser = floraeUserCacheService.findByEmailOrUsername(floraeUserLoginDTO.getEmail(), floraeUserLoginDTO.getUsername());

        if (floraeUser == null)
        {
            return false;
        }

        try {
            return authManager.authenticate(new UsernamePasswordAuthenticationToken(floraeUser.getUsername(), floraeUserLoginDTO.getPassword())).isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }

    public String generateAccessToken(String username) {
        return jwtService.generateAccessToken(username, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String refreshAccessToken(String refreshToken)
    {
        String username = jwtService.extractUsername(refreshToken);

        if (jwtService.validateRefreshToken(refreshToken)){
            return generateAccessToken(username);
        }

        return null;
    }

    public String getRefreshToken(String username)
    {
        return jwtService.generateRefreshToken(username, REFRESH_TOKEN_EXPIRATION_TIME);
    }
}
