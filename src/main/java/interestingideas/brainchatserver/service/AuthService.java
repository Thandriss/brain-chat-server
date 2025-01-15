package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.dto.RegDto;
import interestingideas.brainchatserver.dto.UserDto;
import interestingideas.brainchatserver.exception.RestException;
import interestingideas.brainchatserver.mail.EmailSender;
import interestingideas.brainchatserver.model.ConfState;
import interestingideas.brainchatserver.model.ConfirmationCode;
import interestingideas.brainchatserver.model.Role;
import interestingideas.brainchatserver.model.User;
import interestingideas.brainchatserver.repository.ConfCodeRepository;
import interestingideas.brainchatserver.repository.UsersRepository;
import interestingideas.brainchatserver.respreq.AuthRequest;
import interestingideas.brainchatserver.respreq.AuthResponse;
import interestingideas.brainchatserver.respreq.RegisterRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
//    @Value("${spring.mail.password}")
//    private String sendGridKey;
    @Value("${spring.mail.sender}")
    private String senderEmail;

    private final UsersRepository userRepository;
    private final ConfCodeRepository confCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final Map<String, Boolean> adminStorage = new HashMap<>();
    private final EmailSender emailSender;
    @Transactional
    public UserDto register(RegisterRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.USER)
                .createTimeStamp(LocalDateTime.now())
                .hashPassword(passwordEncoder.encode(request.getPassword()))
                .state(ConfState.NOT_CONFIRMED)
                .build();
        userRepository.save(user);
        String confCode = getNewConfirmationCode();
        ConfirmationCode toSaveCode = ConfirmationCode.builder()
                .code(confCode)
                .userId(user)
                .expiredDateTime(LocalDateTime.now().plusDays(1))
                .build();
        confCodeRepository.save(toSaveCode);
        emailSender.send( senderEmail, request.getEmail(), "Confirmation code is: " + confCode);
        return UserDto.from(user);
    }

    @Transactional
    public UserDto confirm(String confirmCode, RegDto signUpDto) {
        ConfirmationCode confirmationCode = confCodeRepository
                .findByCodeAndExpiredDateTimeAfter(confirmCode, LocalDateTime.now())
                .orElseThrow(() -> new RestException(HttpStatus.NOT_FOUND, "Code not found or is expired"));
        User user = userRepository.findByEmail(signUpDto.getEmail())
                .orElseThrow(() -> new RestException(HttpStatus.NOT_FOUND, "User by email not found"));
        if (confirmationCode != null && Objects.equals(confirmationCode.getCode(), confirmCode)) {
            user.setState(ConfState.CONFIRMED);
            userRepository.save(user);
        }
        return UserDto.from(user);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public AuthResponse login(@NonNull AuthRequest authRequest) throws RestException {
        final User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow();
        if (passwordEncoder.matches(authRequest.getPassword(), user.getHashPassword())) {
            final String accessToken = jwtService.generateToken(user);
            final String refreshToken = jwtService.generateRefreshToken(user);
            boolean isAdmin = false;
            if (Objects.equals(user.getRole().getAuthority(), Role.ADMIN.getAuthority())) {
                refreshStorage.put(user.getEmail(), refreshToken);
                adminStorage.put("admin", true);
                isAdmin = true;
                return new AuthResponse(accessToken, refreshToken, isAdmin);
            } else {
                refreshStorage.put(user.getEmail(), refreshToken);
                return new AuthResponse(accessToken, refreshToken, isAdmin);
            }
        } else {
            throw new RestException(HttpStatus.UNAUTHORIZED, "Wrong password");
        }
    }

    private String getNewConfirmationCode() {
        Random rand = new Random();
        return String.format("%04d", rand.nextInt(9999));
    }
}
