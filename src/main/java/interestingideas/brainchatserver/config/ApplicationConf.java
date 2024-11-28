package interestingideas.brainchatserver.config;

import interestingideas.brainchatserver.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ApplicationConf {

    private final UsersRepository usersRepository;
    @Value("${mail.sender.username}")
    private String mailSenderUsername;
    @Value("${mail.sender.password}")
    private String mailSenderPassword;
    @Value("${mail.sender.host}")
    private String mailSenderHost;
    @Value("${mail.sender.protocol}")
    private String mailSenderTransportProtocol;
    @Value("${mail.sender.smtp.auth}")
    private String mailSenderSmtpAuth;
    @Value("${mail.sender.smtp.starttls.enable}")
    private String mailSenderSmtpStartTlsEnable;
    @Value("${mail.sender.debug}")
    private String mailSenderMailDebug;
    @Value("${mail.sender.port}")
    private Integer mailSenderPort;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    };

    @Bean
    public AuthenticationProvider authenticationProvider () {
        DaoAuthenticationProvider authProv = new DaoAuthenticationProvider();
        authProv.setUserDetailsService(userDetailsService());
        authProv.setPasswordEncoder(passwordEnc());
        return authProv;
    }

    @Bean
    public PasswordEncoder passwordEnc() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailSenderHost);
        mailSender.setPort(mailSenderPort);

        mailSender.setUsername(mailSenderUsername);
        mailSender.setPassword(mailSenderPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.sender.transport.protocol", mailSenderTransportProtocol);
        props.put("mail.sender.smtp.auth", mailSenderSmtpAuth);
        props.put("mail.sender.smtp.starttls.enable", mailSenderSmtpStartTlsEnable);
        props.put("mail.sender.debug", mailSenderMailDebug);

        return mailSender;
    }

}
