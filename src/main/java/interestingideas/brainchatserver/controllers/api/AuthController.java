package interestingideas.brainchatserver.controllers.api;

import interestingideas.brainchatserver.dto.RegDto;
import interestingideas.brainchatserver.dto.UserDto;
import interestingideas.brainchatserver.respreq.AuthRequest;
import interestingideas.brainchatserver.respreq.AuthResponse;
import interestingideas.brainchatserver.respreq.RegisterRequest;
import interestingideas.brainchatserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/register")
    public UserDto register (@RequestBody RegisterRequest request) {
      return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/confirm/{confirm_code}")
    public UserDto confirm (@PathVariable("confirm_code") String confirmCode,
                                            @RequestBody @Valid RegDto regDto) {
        return authService.confirm(confirmCode, regDto);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate (@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
