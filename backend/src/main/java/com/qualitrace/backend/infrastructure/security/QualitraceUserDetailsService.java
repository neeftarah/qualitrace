package com.qualitrace.backend.infrastructure.security;

import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.repository.UserRepository;
import com.qualitrace.backend.domain.type.UserStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class QualitraceUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public QualitraceUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + login));

        return new QualitracePrincipal(
                user.id(),
                user.login(),
                user.password(),
                user.roles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList(),
                user.status() == UserStatus.LOCKED,
                user.status() == UserStatus.ARCHIVED
        );
    }
}