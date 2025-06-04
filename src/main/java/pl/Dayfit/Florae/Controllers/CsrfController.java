package pl.Dayfit.Florae.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is for generating csrf token for the first time
 * <p> Will be used later on
 */
@RestController
public class CsrfController {
    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken)
    {
        return ResponseEntity.ok(csrfToken);
    }
}
