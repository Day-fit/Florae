package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;

@Service
@RequiredArgsConstructor
public class FloraeUserService {
    private final FloraeUserRepository floraeUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
}
