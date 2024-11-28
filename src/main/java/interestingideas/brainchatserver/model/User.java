package interestingideas.brainchatserver.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createTimeStamp;
    @Column(name = "password", length = 100, nullable = false)
    private String hashPassword;
    @Column(name = "username", length = 50, nullable = false)
    private String name;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Column(name = "user_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConfState state;
    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return hashPassword;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
//        UserDetails.super.isAccountNonExpired()
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
//        UserDetails.super.isAccountNonLocked()
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
//        UserDetails.super.isCredentialsNonExpired()
        return true;
    }

    @Override
    public boolean isEnabled() {
//        UserDetails.super.isEnabled()
        return true;
    }
}

