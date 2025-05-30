package pl.Dayfit.Florae.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CsrfController {
    @GetMapping
    public ResponseEntity<?> getCsrfToken(CsrfToken csrfToken)
    {
        return ResponseEntity.ok(csrfToken);
    }
}
